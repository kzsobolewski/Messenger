package pl.kzsobolewski.mymessenger.messages

import SomeoneChatItem
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
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
        toUser = intent.getParcelableExtra(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username
        listenForMessages()
        send_button_chatlog.setOnClickListener {
            performSend()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance()
                .getReference("/user-messages/$fromId/$toId")
        var isFirstMessage = true
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java)
                Log.d(CHATLOG_TAG, "message sent: " + message?.text)
                if (message != null) {
                    isFirstMessage = if (message.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(YourChatItem(message.text))
                        true
                    } else {
                        adapter.add(SomeoneChatItem(message.text, toUser!!, isFirstMessage))
                        false
                    }
                }
                messages_recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }


    private fun performSend() {
        val text = edittext_chatlog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid ?: return
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid
        val ref =
                FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val refOtherUser =
                FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val message =
                Message(ref.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        ref.setValue(message).addOnSuccessListener {
            edittext_chatlog.text.clear()
            messages_recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
        }
        if (toId != fromId)
            refOtherUser.setValue(message)

        FirebaseDatabase.getInstance()
                .getReference("/latest-messages/$fromId/$toId").setValue(message)
        FirebaseDatabase.getInstance()
                .getReference("/latest-messages/$toId/$fromId").setValue(message)
    }
}



