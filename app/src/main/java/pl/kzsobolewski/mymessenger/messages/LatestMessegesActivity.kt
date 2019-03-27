package pl.kzsobolewski.mymessenger.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messeges.*
import kotlinx.android.synthetic.main.activity_new_message.*
import pl.kzsobolewski.mymessenger.R
import pl.kzsobolewski.mymessenger.logIn.RegisterActivity
import pl.kzsobolewski.mymessenger.models.User

class LatestMessegesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
        val LATEST_ACTIVITY_TAG = "LATEST_ACTIVITY_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messeges)
        dummy()
        fetchCurrentUser()
        verifyIfUserIsLogged()

    }

    class LatestMessagesRow: Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
        }

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }
    }

    private fun dummy(){
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(LatestMessagesRow())
        adapter.add(LatestMessagesRow())
        adapter.add(LatestMessagesRow())
        adapter.add(LatestMessagesRow())
        recyclerView_latest_messages.adapter = adapter
    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d(LATEST_ACTIVITY_TAG, "fetched currnet user: " + currentUser?.username)
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.new_message_menu -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.signout_message_menu -> {
                val intent  = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun verifyIfUserIsLogged(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

}
