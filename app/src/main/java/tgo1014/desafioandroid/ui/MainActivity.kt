package tgo1014.desafioandroid.ui

import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.GridLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import tgo1014.desafioandroid.R
import tgo1014.desafioandroid.adapters.ShotRecyclerAdapter
import tgo1014.desafioandroid.adapters.ShotRecyclerPresenter
import tgo1014.desafioandroid.base.BaseMvpActivity
import tgo1014.desafioandroid.contracts.MainContract
import tgo1014.desafioandroid.listeners.GridEndlessRecyclerViewScrollListener
import tgo1014.desafioandroid.model.MainModelImpl
import tgo1014.desafioandroid.model.objects.Shot
import tgo1014.desafioandroid.presenters.MainPresenterImpl

class MainActivity : BaseMvpActivity<MainContract.MainPresenter, MainContract.MainView>(), MainContract.MainView {

    override fun onRetainCustomNonConfigurationInstance(): Any = mainPresenter!!


    private var mainPresenter: MainContract.MainPresenter? = null
    private lateinit var mainRecyclerAdapter: ShotRecyclerAdapter
    private var mainRecyclerShotList: MutableList<Shot> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainToolbar)
        supportActionBar?.title = getString(R.string.app_name)

        attachPresenter()
        mainSwipeContainer.setOnRefreshListener { mainPresenter?.onSwipeToRefresh() }
    }

    override fun onDestroy() {
        mainPresenter?.onDestroy(mainRecyclerShotList)
        mainPresenter?.detachView()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mainPresenter?.onOrderMenuItemClick(item.itemId)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun attachPresenter() {
        mainPresenter = lastCustomNonConfigurationInstance as MainContract.MainPresenter?
        if (mainPresenter == null) {
            mainPresenter = MainPresenterImpl(MainModelImpl())
        }
        mainPresenter?.attachView(this)
    }

    override fun showLoading() {
        mainRecycler.visibility = View.GONE
        mainProgressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        mainRecycler.visibility = View.VISIBLE
        mainProgressBar.visibility = View.GONE
    }

    override fun showLoadingToolbar() {
        mainTollbarProgressBar.visibility = View.VISIBLE
    }

    override fun hideLoadingToolbar() {
        mainTollbarProgressBar.visibility = View.GONE
    }

    override fun showShots(shotList: List<Shot>) {
        mainRecyclerShotList.addAll(shotList)
        mainRecyclerAdapter = ShotRecyclerAdapter(ShotRecyclerPresenter(mainRecyclerShotList))
        mainRecycler.adapter = mainRecyclerAdapter

        val gridLayoutManager = GridLayoutManager(this, calcImageGridSizeByOrientation())
        mainRecycler.layoutManager = gridLayoutManager

        mainRecycler.addOnScrollListener(
                GridEndlessRecyclerViewScrollListener(
                        gridLayoutManager,
                        GridEndlessRecyclerViewScrollListener.DataLoader {
                            mainPresenter?.loadMoreShots()
                            true
                        }))
    }

    override fun onLoadMoreShots(shotList: List<Shot>) {
        mainRecyclerShotList.addAll(shotList)
        mainRecyclerAdapter.notifyItemRangeInserted(mainRecyclerShotList.size - shotList.size, mainRecyclerShotList.size)
    }

    override fun showError(error: String) {
        Snackbar
                .make(findViewById(R.id.mainLinearLayout), getString(R.string.str_error) + ":" + error, Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.str_try_again), { mainPresenter?.onSnackBarClicked() })
                .show()
    }

    private fun calcImageGridSizeByOrientation(): Int {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            return 3
        return 2
    }

    override fun refreshDone() {
        mainSwipeContainer.isRefreshing = false
    }
}


