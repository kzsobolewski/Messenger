package pl.kzsobolewski.mymessenger.messages

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_row_someone.view.*
import kotlinx.android.synthetic.main.chat_row_you.view.*
import pl.kzsobolewski.mymessenger.R
import pl.kzsobolewski.mymessenger.models.Message
import pl.kzsobolewski.mymessenger.models.User

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val CHATLOG_TAG = "CHATLOG_TAG"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        messages_recyclerview_chatlog.adapter = adapter
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username
        listenForMessages()
        send_button_chatlog.setOnClickListener{
            performSend()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java)
                Log.d(CHATLOG_TAG, "message sent: " + message?.text)
                if(message != null) {
                    if (message.fromId == FirebaseAuth.getInstance().uid)
                        adapter.add(YourChatItem(message.text))
                    else {
                        adapter.add(SomeoneChatItem(message.text,toUser!!))
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }



    private fun performSend() {
        val text = edittext_chatlog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        if(fromId == null) return
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val refOtherUser = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val message = Message(ref.key!!, text,  fromId, toId, System.currentTimeMillis() / 1000)
        ref.setValue(message).addOnSuccessListener {
            Log.d(CHATLOG_TAG, "Saved our chat message " + ref.key)
            edittext_chatlog.text.clear()
            messages_recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
        }
        refOtherUser.setValue(message)
    }
}


class SomeoneChatItem(val text:String, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.from_text_chatrow.text = text
        val uri = user.profileImageUrl
        val avatar_imgView = viewHolder.itemView.avatar_chatrow
        Picasso.get().load(uri).into(avatar_imgView)
    }
    override fun getLayout(): Int {
        return R.layout.chat_row_someone
    }
}
class YourChatItem(val text:String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.your_text_chatrow.text = text
    }
    override fun getLayout(): Int {
        return R.layout.chat_row_you
    }
}
