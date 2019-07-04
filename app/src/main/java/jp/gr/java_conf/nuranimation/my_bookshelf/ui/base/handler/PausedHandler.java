package jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.handler;

import android.os.Handler;
import android.os.Message;
import java.util.LinkedList;
import java.util.Queue;

public class PausedHandler extends Handler {
    private Queue<Message> mQueue = new LinkedList<>();
    private boolean isPaused;

    public PausedHandler(){}

    public void resume() {
        isPaused = false;
        while(!mQueue.isEmpty()){
            Message msg = mQueue.poll();
            if(msg != null){
                dispatchMessage(msg);
            }
        }
    }

    public void pause() {
        isPaused = true;
    }


    @Override
    public void dispatchMessage(Message msg){
        if(isPaused){
            final Message copied = Message.obtain(msg);
            mQueue.offer(copied);
        }else{
            super.dispatchMessage(msg);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
    }
}

