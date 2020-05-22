import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_row_someone.view.*
import pl.kzsobolewski.mymessenger.R
import pl.kzsobolewski.mymessenger.models.User

class SomeoneChatItem(val text: String, val user: User, val isNew: Boolean) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.from_text_chatrow.text = text
        val uri = user.profileImageUrl
        if (isNew) {
            var avatar_imgView = viewHolder.itemView.avatar_chatrow
            Picasso.get().load(uri).resize(100, 100)
                    .centerCrop()
                    .into(avatar_imgView)
        }
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_someone
    }
}