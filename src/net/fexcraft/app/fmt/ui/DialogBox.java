package net.fexcraft.app.fmt.ui;

import static org.liquidengine.legui.event.MouseClickEvent.MouseClickAction.CLICK;

import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Dialog;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.listener.MouseClickEventListener;

import net.fexcraft.app.fmt.FMTB;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class DialogBox {
	
	public static final void show(String title, String text0, String text1, DialogTask but0, DialogTask but1, String... text){
		if(title == null) title = "dialogbox.title.default";
        Dialog dialog = new Dialog(UserInterfaceUtils.translate(title), 400, 70 + (text.length * 25)); dialog.setResizable(false);
        for(int i = 0; i < text.length; i++){
        	Label label = new Label(UserInterfaceUtils.translate(text[i]), 10, 10 + (i * 25), 380, 20); dialog.getContainer().add(label);
        }
        if(text0 != null){
            Button button0 = new Button(UserInterfaceUtils.translate(text0 == null ? "dialogbox.button.confirm" : text0), 10, 20 + (text.length * 25), 100, 20);
            button0.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) e -> {
            	if(CLICK == e.getAction()){ if(but0 != null) but0.process(); dialog.close(); }
            });
            dialog.getContainer().add(button0);
        }
        if(text1 != null){
            Button button1 = new Button(UserInterfaceUtils.translate(text1 == null ? "dialogbox.button.cancel" : text1), 120, 20 + (text.length * 25), 100, 20);
            button1.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) e -> {
            	if(CLICK == e.getAction()){ if(but1 != null) but1.process(); dialog.close(); }
            });
            dialog.getContainer().add(button1);
        }
        dialog.show(FMTB.frame);
	}
	
	public static final void showYN(String title, DialogTask but0, DialogTask but1, String... text){
		show(title, "dialogbox.button.yes", "dialogbox.button.no", but0, but1, text);
	}
	
	public static final void showOC(String title, DialogTask but0, DialogTask but1, String... text){
		show(title, "dialogbox.button.ok", "dialogbox.button.cancel", but0, but1, text);
	}
	
	public static final void showOK(String title, DialogTask but0, DialogTask but1, String... text){
		show(title, "dialogbox.button.ok", null, but0, but1, text);
	}
	
	@FunctionalInterface
	public static interface DialogTask {
		
		public void process();
		
	}

}
