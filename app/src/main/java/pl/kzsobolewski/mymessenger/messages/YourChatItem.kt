package pl.kzsobolewski.mymessenger.messages

import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_row_you.view.*
import pl.kzsobolewski.mymessenger.R

class YourChatItem(val text: String) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.your_text_chatrow.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_you
    }
}