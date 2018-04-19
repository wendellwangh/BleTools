package com.ccl.bletools.utils;import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class Messager {
	public static final int KMESSAGE_DURATION_SHORT = Toast.LENGTH_SHORT;
	public static final int KMESSAGE_DURATION_LONG = Toast.LENGTH_LONG;
	
	private static final int KMESSAGE_SHOW = 1;
	private static final int KMESSAGE_CANCEL = 2;
	
	private static Messager gMessager = null;
	
	public static boolean Initialize(Context aContext){
		if (gMessager == null){
			if (aContext != null){
				gMessager = new Messager(aContext);
			}
		}
		
		return gMessager != null;
	}
	
	public static boolean Show(Object aMessage, int aDuration){
		if (aMessage != null && gMessager != null){
			return gMessager.show(aMessage, aDuration);
		}
		
		return false;
	}
	
	public static boolean Show(Object aMessage){
		if (gMessager != null){
			return gMessager.show(aMessage, KMESSAGE_DURATION_LONG);
		}
		
		return false;
	}
	
	public static boolean Cancel(){
		if (gMessager != null){
			return gMessager.cancel();
		}
		
		return false;
	}
	
	public static void ShowInfo(int errorCode){
		
	}
	
	private Context iContext = null;
	
	private Toast iToast = null;
	
	private Messager(Context aContext){
		iContext = aContext;
		
	}
	
	private Handler iHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (msg != null){
				if (msg.what == KMESSAGE_SHOW){
					if (iToast == null){
						if (msg.obj instanceof Integer){
							iToast = Toast.makeText(iContext, (Integer)msg.obj, Toast.LENGTH_SHORT);
						}else if (msg.obj instanceof CharSequence){
							iToast = Toast.makeText(iContext, (CharSequence)msg.obj, Toast.LENGTH_SHORT);
						}
					}else{
						if (msg.obj instanceof Integer){
							iToast.setText((Integer)msg.obj);
						}else if (msg.obj instanceof CharSequence){
							iToast.setText((CharSequence)msg.obj);
						}
					}
					
					if (iToast != null){
						iToast.setDuration(msg.arg1);
						
						iToast.show();
					}
				}else if (msg.what == KMESSAGE_CANCEL){
					if (iToast != null){
						iToast.cancel();
					}
				}
			}
		}
	};
	
	public boolean show(Object aMessage, int aDuration){
		if (aMessage != null){
			return iHandler.sendMessage(Message.obtain(iHandler, KMESSAGE_SHOW, aDuration, aDuration, aMessage));
		}
		
		return false;
	}
	
	public boolean cancel(){
		if (iHandler != null){
			return iHandler.sendMessage(Message.obtain(iHandler, KMESSAGE_CANCEL));
		}
		
		return false;
	}
	
}
