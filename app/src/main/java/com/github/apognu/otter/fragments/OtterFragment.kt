package com.github.apognu.otter.fragments

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.github.apognu.otter.repositories.HttpUpstream
import com.github.apognu.otter.repositories.Repository
import com.github.apognu.otter.utils.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_artists.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class OtterAdapter<D, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
  var data: MutableList<D> = mutableListOf()

  init {
    super.setHasStableIds(true)
  }

  abstract override fun getItemId(position: Int): Long
}

abstract class OtterFragment<D : Any, A : OtterAdapter<D, *>> : Fragment() {
  companion object {
    const val OFFSCREEN_PAGES = 20
  }

  abstract val viewRes: Int
  abstract val recycler: RecyclerView
  open val layoutManager: RecyclerView.LayoutManager get() = LinearLayoutManager(context)
  open val alwaysRefresh = true

  lateinit var repository: Repository<D, *>
  lateinit var adapter: A

  private var moreLoading = false
  private var listener: Job? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(viewRes, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    recycler.layoutManager = layoutManager
    (recycler.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    recycler.adapter = adapter

    (repository.upstream as? HttpUpstream<*, *>)?.let { upstream ->
      if (upstream.behavior == HttpUpstream.Behavior.Progressive) {
        recycler.setOnScrollChangeListener { _, _, _, _, _ ->
          val offset = recycler.computeVerticalScrollOffset()

          if (!moreLoading && offset > 0 && needsMoreOffscreenPages()) {
            moreLoading = true

            fetch(Repository.Origin.Network.origin, adapter.data.size)
          }
        }
      }
    }

    if (listener == null) {
      listener = lifecycleScope.launch(IO) {
        EventBus.get().collect { event ->
          if (event is Event.ListingsChanged) {
            withContext(Main) {
              swiper?.isRefreshing = true
              fetch(Repository.Origin.Network.origin)
            }
          }
        }
      }
    }

    fetch(Repository.Origin.Cache.origin)

    if (alwaysRefresh && adapter.data.isEmpty()) {
      fetch(Repository.Origin.Network.origin)
    }
  }

  override fun onResume() {
    super.onResume()

    swiper?.setOnRefreshListener {
      fetch(Repository.Origin.Network.origin)
    }
  }

  fun update() {
    swiper?.isRefreshing = true
    fetch(Repository.Origin.Network.origin)
  }

  private fun isAnyNetworkAvailable(): Boolean {
    val connectivityManager = context?.getSystemService(ConnectivityManager::class.java)
    val currentNetwork = connectivityManager?.activeNetwork
    val caps = connectivityManager?.getNetworkCapabilities(currentNetwork)

    return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
  }

  open fun onDataFetched(data: List<D>) {}

  private fun fetch(
    upstreams: Int = Repository.Origin.Network.origin, size: Int = 0
  ) {

    var first = size == 0
    moreLoading = true

    val isNetworkAvailable = isAnyNetworkAvailable()

    // Take data from cache if there's no available network
    if (!isNetworkAvailable) {
      repository.fetch(Repository.Origin.Cache.origin, size)
        .untilNetwork(lifecycleScope, IO) { data, isCache, page, hasMore ->
          lifecycleScope.launch(Main) {
            if (isCache) {
              moreLoading = false

              adapter.data = data.toMutableList()
              adapter.notifyDataSetChanged()
            }

            if (first) {
              adapter.data.clear()
            }

            onDataFetched(data)

            adapter.data.addAll(data)

            if (hasMore) {
              (repository.upstream as? HttpUpstream<*, *>)?.let { upstream ->
                if (!isCache && upstream.behavior == HttpUpstream.Behavior.Progressive) {
                  if (first || needsMoreOffscreenPages()) {
                    fetch(Repository.Origin.Network.origin, adapter.data.size)
                  } else {
                    moreLoading = false
                  }
                } else {
                  moreLoading = false
                }
              }
            }

            (repository.upstream as? HttpUpstream<*, *>)?.let { upstream ->
              when (upstream.behavior) {
                HttpUpstream.Behavior.Progressive -> if (!hasMore || !moreLoading) swiper?.isRefreshing =
                  false
                HttpUpstream.Behavior.AtOnce -> if (!hasMore) swiper?.isRefreshing = false
                HttpUpstream.Behavior.Single -> if (!hasMore) swiper?.isRefreshing = false
              }
            }

            when (first) {
              true -> {
                adapter.notifyDataSetChanged()
                first = false
              }

              false -> adapter.notifyItemRangeInserted(adapter.data.size, data.size)
            }
          }
        }
      return
    }

    if (!moreLoading && upstreams == Repository.Origin.Network.origin) {
      lifecycleScope.launch(Main) {
        swiper?.isRefreshing = true
      }
    }

    repository.fetch(upstreams, size)
      .untilNetwork(lifecycleScope, IO)
      { data, isCache, _, hasMore ->
        if (isCache && data.isEmpty()) {
          moreLoading = false

          return@untilNetwork fetch(Repository.Origin.Network.origin)
        }

        lifecycleScope.launch(Main) {
          if (isCache) {
            moreLoading = false

            adapter.data = data.toMutableList()
            adapter.notifyDataSetChanged()

            return@launch
          }

          if (first) {
            adapter.data.clear()
          }

          onDataFetched(data)

          adapter.data.addAll(data)

          withContext(IO) {
            try {
              repository.cacheId?.let { cacheId ->
                Cache.set(
                  context,
                  cacheId,
                  Gson().toJson(repository.cache(adapter.data)).toByteArray()
                )
              }
            } catch (e: ConcurrentModificationException) {
            }
          }

          if (hasMore) {
            (repository.upstream as? HttpUpstream<*, *>)?.let { upstream ->
              if (!isCache && upstream.behavior == HttpUpstream.Behavior.Progressive) {
                if (first || needsMoreOffscreenPages()) {
                  fetch(Repository.Origin.Network.origin, adapter.data.size)
                } else {
                  moreLoading = false
                }
              } else {
                moreLoading = false
              }
            }
          }

          (repository.upstream as? HttpUpstream<*, *>)?.let { upstream ->
            when (upstream.behavior) {
              HttpUpstream.Behavior.Progressive -> if (!hasMore || !moreLoading) swiper?.isRefreshing =
                false
              HttpUpstream.Behavior.AtOnce -> if (!hasMore) swiper?.isRefreshing = false
              HttpUpstream.Behavior.Single -> if (!hasMore) swiper?.isRefreshing = false
            }
          }

          when (first) {
            true -> {
              adapter.notifyDataSetChanged()
              first = false
            }

            false -> adapter.notifyItemRangeInserted(adapter.data.size, data.size)
          }
        }
      }
  }

  private fun needsMoreOffscreenPages(): Boolean {
    view?.let {
      val offset = recycler.computeVerticalScrollOffset()
      val left = recycler.computeVerticalScrollRange() - recycler.height - offset

      return left < (recycler.height * OFFSCREEN_PAGES)
    }

    return false
  }
}
