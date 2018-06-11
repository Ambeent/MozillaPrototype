package com.wireless.ambeent.mozillaprototype.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wireless.ambeent.mozillaprototype.R;
import com.wireless.ambeent.mozillaprototype.helpers.Constants;
import com.wireless.ambeent.mozillaprototype.pojos.MessageObject;

import java.util.List;
/**
 * Created by Ambeent Wireless.
 */
public class ChatAdapter  extends RecyclerView.Adapter<ChatAdapter.MyViewHolder>{

    private static final String TAG = "ChatAdapter";

    private Context mContext;
    private List<MessageObject> mMessageObjectList;


    public ChatAdapter(Context mContext, List<MessageObject> mMessageObjectList) {
        this.mContext = mContext;
        this.mMessageObjectList = mMessageObjectList;


    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Creates the views from layout xml.
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_chat, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        MessageObject messageObject = mMessageObjectList.get(position);
        String sender = messageObject.getSender();
        String message = messageObject.getMessage();

        String receiver = messageObject.getReceiver();

        if(Constants.PHONE_NUMBER.equalsIgnoreCase(receiver)){
            //The user is the receiver. So the user receives a private message
            holder.senderContainerCardView.setVisibility(View.VISIBLE);
            holder.senderContainerCardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorLightGray));
            holder.senderNameTextView.setText(sender);
            holder.senderMessageTextView.setText(message);

            holder.ownerContainerCardView.setVisibility(View.GONE);

        }else if(Constants.PHONE_NUMBER.equalsIgnoreCase(sender) && !receiver.equalsIgnoreCase(mContext.getResources().getString(R.string.message_with_no_receiver))){
            //The user is the sender and the receiver is not null. The user sends a private message
            holder.senderContainerCardView.setVisibility(View.GONE);

            holder.ownerContainerCardView.setVisibility(View.VISIBLE);
            holder.ownerTargetTextView.setVisibility(View.VISIBLE);
            holder.ownerTargetTextView.setText(mContext.getResources().getString(R.string.message_to, receiver));
            holder.ownerMessageTextView.setText(message);

        }else if(Constants.PHONE_NUMBER.equalsIgnoreCase(sender) && receiver.equalsIgnoreCase(mContext.getResources().getString(R.string.message_with_no_receiver))){
            //The user is the sender and the receiver is null. So the user receives a group message
            holder.senderContainerCardView.setVisibility(View.GONE);

            holder.ownerContainerCardView.setVisibility(View.VISIBLE);
            holder.ownerMessageTextView.setText(message);
            holder.ownerTargetTextView.setVisibility(View.GONE);

        } else {
            //The user received a regular group message
            holder.senderContainerCardView.setVisibility(View.VISIBLE);
            holder.senderContainerCardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorWhite));
            holder.senderNameTextView.setText(sender);
            holder.senderMessageTextView.setText(message);

            holder.ownerContainerCardView.setVisibility(View.GONE);
        }


    }

    // Return the size of the dataset
    @Override
    public int getItemCount() {
        return mMessageObjectList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {


        public TextView senderNameTextView, senderMessageTextView, ownerMessageTextView, ownerTargetTextView;

        public CardView senderContainerCardView, ownerContainerCardView;


        public MyViewHolder(View view) {
            super(view);

            senderNameTextView = (TextView) view.findViewById(R.id.textView_SenderName);
            senderMessageTextView = (TextView) view.findViewById(R.id.textView_SenderMessage);
            ownerMessageTextView = (TextView) view.findViewById(R.id.textView_OwnerMessage);
            ownerTargetTextView = (TextView) view.findViewById(R.id.textView_OwnerTarget);


            senderContainerCardView = (CardView) view.findViewById(R.id.cardView_MsgContainer);
            ownerContainerCardView = (CardView) view.findViewById(R.id.cardView_OwnerMsgContainer);
        }



    }

}
