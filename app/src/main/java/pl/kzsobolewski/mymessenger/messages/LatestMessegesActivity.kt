package pl.kzsobolewski.mymessenger.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messeges.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import pl.kzsobolewski.mymessenger.R
import pl.kzsobolewski.mymessenger.logIn.RegisterActivity
import pl.kzsobolewski.mymessenger.models.Message
import pl.kzsobolewski.mymessenger.models.User

class LatestMessegesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
        val LATEST_ACTIVITY_TAG = "LATEST_ACTIVITY_TAG"
    }
        private val adapter = GroupAdapter<ViewHolder>()
        private val latestMessagesMap = HashMap<String, Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_latest_messeges)
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessagesRow
            intent.putExtra(NewMessageActivity.USER_KEY, row.otherUser)
            startActivity(intent)
        }
        listenForLatestMessages()
        fetchCurrentUser()
        verifyIfUserIsLogged()
        add_fab_latest_mesaages.setOnClickListener{
            val intent = Intent(this, NewMessageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun refreshRecycler(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessagesRow(it))
        }
    }

    private fun listenForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java) ?: return
                latestMessagesMap[p0.key!!] = message
                refreshRecycler()
                }
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java) ?: return
                latestMessagesMap[p0.key!!] = message
                refreshRecycler()
                }
        })
        recyclerView_latest_messages.adapter = adapter
    }

    class LatestMessagesRow(val chatMessage: Message): Item<ViewHolder>() {
        var otherUser: User? = null
            private set
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.message_textView_latest_messages_row.text = chatMessage.text

            val chatPartnerId: String
            if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                chatPartnerId = chatMessage.toId
            } else {
                chatPartnerId = chatMessage.fromId
            }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent( object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(p0: DataSnapshot) {
                    otherUser = p0.getValue(User::class.java)
                    viewHolder.itemView.username_textView_latest_messages.text = otherUser?.username
                    Picasso.get().load(otherUser?.profileImageUrl)
                            .into(viewHolder.itemView.avatar_imageView_latest_messages_row)
                }
            })
        }

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }
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
            R.id.signout_message_menu -> {
                val intent  = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun verifyIfUserIsLogged(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
