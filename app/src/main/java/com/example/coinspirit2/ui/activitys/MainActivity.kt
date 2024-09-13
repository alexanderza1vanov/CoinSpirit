package com.example.coinspirit2.ui.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coinspirit2.R
import com.example.coinspirit2.data.model.PortfolioRVModel
import com.example.coinspirit2.data.repository.CurrencyRepository
import com.example.coinspirit2.ui.adapters.PortfolioRVAdapter
import com.example.coinspirit2.ui.fragments.CurrencyDetailFragment
import com.example.coinspirit2.ui.fragments.SearchFragment
import com.example.coinspirit2.ui.fragments.SettingsFragment
import com.example.coinspirit2.ui.interfaces.DataTransfer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.util.Random

class MainActivity : AppCompatActivity(), DataTransfer {

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var portfolioRVAdapter: PortfolioRVAdapter
    private lateinit var portfolioList: MutableList<PortfolioRVModel>
    private lateinit var totalValueTextView: TextView
    private lateinit var profitTextView: TextView
    private lateinit var currencyRepository: CurrencyRepository
    private val df2 = DecimalFormat("#.##")

    private val updateInterval = 5000L // Интервал обновления в миллисекундах (5 секунд)
    private var updateJob: Job? = null

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        recyclerView = findViewById(R.id.main_recyclerview)
//        totalValueTextView = findViewById(R.id.value_portfolio_tv)
//        profitTextView = findViewById(R.id.profit_portfolio_tv)
//        portfolioList = ArrayList()
//        portfolioRVAdapter = PortfolioRVAdapter(this, portfolioList)
//        recyclerView.adapter = portfolioRVAdapter
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        val addImageView: ImageView = findViewById(R.id.add_img)
//        addImageView.setOnClickListener { showSearchDialogFragment() }
//
//        val settingsImageView: ImageView = findViewById(R.id.settings_image)
//        settingsImageView.setOnClickListener { showSettingsDialogFragment() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var clickCount = 0  // Переменная для отслеживания количества нажатий

        recyclerView = findViewById(R.id.main_recyclerview)
        totalValueTextView = findViewById(R.id.value_portfolio_tv)
        profitTextView = findViewById(R.id.profit_portfolio_tv)
        portfolioList = ArrayList()
        portfolioRVAdapter = PortfolioRVAdapter(this, portfolioList)
        recyclerView.adapter = portfolioRVAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val addImageView: ImageView = findViewById(R.id.add_img)
        addImageView.setOnClickListener {
            clickCount++  // Увеличиваем счётчик при каждом нажатии
            if (clickCount >= 2) {
                addImageView.isClickable = false  // Отключаем кликабельность после второго нажатия
            } else {
                showSearchDialogFragment()
            }
        }

        val settingsImageView: ImageView = findViewById(R.id.settings_image)
        settingsImageView.setOnClickListener { showSettingsDialogFragment() }

//        auth = FirebaseAuth.getInstance()
//        db = FirebaseFirestore.getInstance()
//        currencyRepository = CurrencyRepository(this)
//
//        val currentUser: FirebaseUser? = auth.currentUser
//
//        if (currentUser == null) {
//            // Пользователь не авторизован, перенаправляем на LoginActivity
//            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
//            finish() // Завершаем текущую активность
//        } else {
//            // Получаем userId текущего пользователя
//            val userId: String = currentUser.uid
//            val userRef: DocumentReference = db.collection("Users").document(userId)
//
//            // Извлекаем данные пользователя из Firestore
//            userRef.get().addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val document = task.result
//                    if (document.exists()) {
//                        val username = document.getString("username")
//                        Toast.makeText(this, "Welcome, $username", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Toast.makeText(this, "Welcome, ${currentUser.email}", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Error getting user data.", Toast.LENGTH_SHORT).show()
//                }
//            }
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()
            currencyRepository = CurrencyRepository(this)

            val currentUser: FirebaseUser? = auth.currentUser

            if (currentUser == null) {
                // Пользователь не авторизован, перенаправляем на LoginActivity
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish() // Завершаем текущую активность
            } else {
                // Получаем userId текущего пользователя
                val userId: String = currentUser.uid
                val userRef: DocumentReference = db.collection("Users").document(userId)

                // Массив случайных имен
                val names = arrayOf("Годрик", "Вхагар", "Валар Маргулис", "Оптимус Прайм")
                val randomName = names[Random().nextInt(names.size)]

                // Извлекаем данные пользователя из Firestore
                userRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document.exists()) {
                            // Ошибка: вместо имени пользователя используется случайное имя
                            Toast.makeText(this, "Welcome, $randomName", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Welcome, ${currentUser.email}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Error getting user data.", Toast.LENGTH_SHORT).show()
                    }
                }



            // Загрузка транзакций
            userRef.collection("Transactions").get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val item = document.toObject(PortfolioRVModel::class.java)
                        portfolioList.add(item)
                    }
                    portfolioRVAdapter.notifyDataSetChanged()
                    updatePortfolioValue()
                    updatePortfolioProfit()
                    startUpdatingPrices() // Запуск обновления цен
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading transactions: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Добавление ItemTouchHelper для удаления транзакций при свайпе влево
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedItem = portfolioList[position]

                // Удаление элемента из списка и уведомление адаптера
                portfolioList.removeAt(position)
                portfolioRVAdapter.notifyItemRemoved(position)

                // Удаление элемента из Firestore
                removeTransactionFromFirestore(deletedItem)

                // Обновление общей стоимости и профита
                updatePortfolioValue()
                updatePortfolioProfit()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)

    }

    //Удаление транзакции из Firestore
    fun removeTransactionFromFirestore(transaction: PortfolioRVModel) {
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            val userId: String = currentUser.uid
            val userRef: DocumentReference = db.collection("Users").document(userId)

            userRef.collection("Transactions")
                .whereEqualTo("name", transaction.name)
                .whereEqualTo("symbol", transaction.symbol)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Transaction ${document.id} successfully deleted")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error deleting transaction ${document.id}", e)
                                }
                        }
                        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("Firestore", "No matching transactions found")
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error deleting transaction: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Firestore", "Error getting documents", e)
                }
        } else {
            Log.e("Firestore", "User is not authenticated")
        }
    }



    private fun showSearchDialogFragment() {
        val fragmentManager: FragmentManager = supportFragmentManager
        val searchDialogFragment = SearchFragment()
        searchDialogFragment.show(fragmentManager, "search_dialog")
    }
    private fun showSettingsDialogFragment() {
        val fragmentManager: FragmentManager = supportFragmentManager
        val settingsDialogFragment = SettingsFragment.newInstance()
        settingsDialogFragment.show(fragmentManager, "settings_dialog")
    }

    private fun showCurrencyDetailDialog() {
        val dialogFragment = CurrencyDetailFragment.newInstance("Bitcoin", "BTC", 30000.0)
        dialogFragment.show(supportFragmentManager, "CurrencyDetailDialog")
    }

    override fun onAddTransaction(name: String, symbol: String, price: String, quantity: String, purchasePrice: String) {
        val newItem = PortfolioRVModel(name, symbol, price, quantity, purchasePrice)
        portfolioList.add(newItem)
        portfolioRVAdapter.notifyDataSetChanged()
        updatePortfolioValue()
        updatePortfolioProfit()

        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            val userId: String = currentUser.uid
            val userRef: DocumentReference = db.collection("Users").document(userId)

            // Добавляем транзакцию в подколлекцию "Transactions" для текущего пользователя
            userRef.collection("Transactions").add(newItem)
                .addOnSuccessListener {
                    Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error adding transaction: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updatePortfolioValue() {
        var totalValue = 0.0
        for (item in portfolioList) {
            val price = item.price.toDoubleOrNull() ?: 0.0
            val quantity = item.quantity.toDoubleOrNull() ?: 0.0
            totalValue += price * quantity
        }
        totalValueTextView.text = "$${df2.format(totalValue)}"
    }

//    private fun updatePortfolioProfit() {
//        var totalProfit = 0.0
//        for (item in portfolioList) {
//            val currentPrice = item.price.toDoubleOrNull() ?: 0.0
//            val purchasePrice = item.purchasePrice.toDoubleOrNull() ?: 0.0
//            val quantity = item.quantity.toDoubleOrNull() ?: 0.0
//            totalProfit += (currentPrice - purchasePrice) * quantity
//        }
//        profitTextView.text = "$${df2.format(totalProfit)}"
//    }
    private fun updatePortfolioProfit() {
        var totalProfit = 0.0
        for (item in portfolioList) {
            val currentPrice = item.price.toDoubleOrNull() ?: 0.0
            val purchasePrice = item.purchasePrice.toDoubleOrNull() ?: 0.0
            val quantity = item.quantity.toDoubleOrNull() ?: 0.0
            totalProfit += (currentPrice - purchasePrice) * quantity
        }

        // Генерируем случайное число от 1 до 10
        val randomNumber = (2..10).random()

        // Делим итоговую прибыль на случайное число
        val dividedProfit = totalProfit / randomNumber

        // Отображаем результат в TextView
        profitTextView.text = "$${df2.format(dividedProfit)}"

        // Логируем для отладки
        Log.d("ProfitCalculation", "Total profit: $totalProfit, Divided by: $randomNumber, Result: $dividedProfit")
    }

    private fun updateCryptoPrices() {
        currencyRepository.getCurrencyData({ currencyList ->
            val updatedPortfolioList = portfolioList.map { portfolioItem ->
                val updatedPrice = currencyList.find { it.symbol == portfolioItem.symbol }?.price ?: portfolioItem.price.toDouble()
                portfolioItem.copy(price = updatedPrice.toString())
            }
            portfolioList.clear()
            portfolioList.addAll(updatedPortfolioList)
            portfolioRVAdapter.notifyDataSetChanged()
            updatePortfolioValue()
            updatePortfolioProfit()
        }, { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        })
    }

    private fun startUpdatingPrices() {
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                updateCryptoPrices()
                delay(updateInterval)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel() // Отмена корутины при уничтожении активности
    }
}
