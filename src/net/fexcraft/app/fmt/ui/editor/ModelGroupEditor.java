package net.fexcraft.app.fmt.ui.editor;

import static org.liquidengine.legui.event.MouseClickEvent.MouseClickAction.CLICK;

import java.util.ArrayList;

import org.joml.Vector4f;
import org.liquidengine.legui.component.Button;
import org.liquidengine.legui.component.Label;
import org.liquidengine.legui.component.SelectBox;
import org.liquidengine.legui.component.TextInput;
import org.liquidengine.legui.component.Tooltip;
import org.liquidengine.legui.component.event.selectbox.SelectBoxChangeSelectionEvent;
import org.liquidengine.legui.component.optional.align.HorizontalAlign;
import org.liquidengine.legui.event.FocusEvent;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.input.Mouse.MouseButton;
import org.liquidengine.legui.style.Background;

import net.fexcraft.app.fmt.FMTB;
import net.fexcraft.app.fmt.ui.FileSelector;
import net.fexcraft.app.fmt.ui.SettingsBox;
import net.fexcraft.app.fmt.ui.UserInterfaceUtils;
import net.fexcraft.app.fmt.ui.field.ColorField;
import net.fexcraft.app.fmt.ui.field.NumberField;
import net.fexcraft.app.fmt.ui.field.TextField;
import net.fexcraft.app.fmt.utils.Animator;
import net.fexcraft.app.fmt.utils.Animator.Animation;
import net.fexcraft.app.fmt.utils.TextureManager;
import net.fexcraft.app.fmt.utils.TextureUpdate;
import net.fexcraft.app.fmt.wrappers.TurboList;
import net.fexcraft.lib.common.Static;
import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.common.math.Vec3f;

public class ModelGroupEditor extends EditorBase {
	
	private static final int[] texsizes = new int[]{ 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096 };//, 8192 };
	public static NumberField pos_x, pos_y, pos_z, poss_x, poss_y, poss_z;
	public static NumberField rot_x, rot_y, rot_z, exoff_x, exoff_y, exoff_z;
	public static TextInput model_texture, model_name;
	public static SelectBox<Float> m_tex_x, m_tex_y, m_tex_s;
	public static ColorField group_color;
	public static TextInput group_name, group_texture;
	public static SelectBox<Float> g_tex_x, g_tex_y, g_tex_s;
	public static SelectBox<Animation> add_anim;
	public static AnimationsEditorWidget animations;
	private String name_cache;
	
	@SuppressWarnings("unchecked")
	public ModelGroupEditor(){
		super(); int pass = -20;
		EditorWidget model = new EditorWidget(this, translate("editor.model_group.model"), 0, 0, 0, 0);
		model.getContainer().add(new Label(translate("editor.model_group.model.position_full"), 3, pass += 24, 290, 20));
		model.getContainer().add(pos_x = new NumberField(4, pass += 24, 90, 20).setup(Integer.MIN_VALUE, Integer.MAX_VALUE, true, () -> updateModelPos(true)));
		model.getContainer().add(pos_y = new NumberField(102, pass, 90, 20).setup(Integer.MIN_VALUE, Integer.MAX_VALUE, true, () -> updateModelPos(true)));
		model.getContainer().add(pos_z = new NumberField(200, pass, 90, 20).setup(Integer.MIN_VALUE, Integer.MAX_VALUE, true, () -> updateModelPos(true)));
		model.getContainer().add(new Label(translate("editor.model_group.model.position_sixteenth"), 3, pass += 24, 290, 20));
		model.getContainer().add(poss_x = new NumberField(4, pass += 24, 90, 20).setup(Integer.MIN_VALUE, Integer.MAX_VALUE, true, () -> updateModelPos(false)));
		model.getContainer().add(poss_y = new NumberField(102, pass, 90, 20).setup(Integer.MIN_VALUE, Integer.MAX_VALUE, true, () -> updateModelPos(false)));
		model.getContainer().add(poss_z = new NumberField(200, pass, 90, 20).setup(Integer.MIN_VALUE, Integer.MAX_VALUE, true, () -> updateModelPos(false)));
		model.getContainer().add(new Label(translate("editor.model_group.model.rotation"), 3, pass += 24, 290, 20));
		model.getContainer().add(rot_x = new NumberField(4, pass += 24, 90, 20).setup(-360, 360, true, () -> updateModelRot()));
		model.getContainer().add(rot_y = new NumberField(102, pass, 90, 20).setup(-360, 360, true, () -> updateModelRot()));
		model.getContainer().add(rot_z = new NumberField(200, pass, 90, 20).setup(-360, 360, true, () -> updateModelRot()));
		model.getContainer().add(new Label(translate("editor.model_group.model.texture_size"), 3, pass += 24, 290, 20));
		model.getContainer().add(m_tex_x = new SelectBox<>(4, pass += 24, 90, 20));
        for(int size : texsizes) m_tex_x.addElement((float)size);
        m_tex_x.getSelectBoxElements().forEach(elm -> elm.getStyle().setFontSize(20f));
        m_tex_x.setVisibleCount(10); m_tex_x.setElementHeight(20);
        m_tex_x.getSelectionButton().getStyle().setFontSize(20f);
        m_tex_x.addSelectBoxChangeSelectionEventListener(event -> updateModelTexSize(event, true));
		model.getContainer().add(m_tex_y = new SelectBox<>(102, pass, 90, 20));
        for(int size : texsizes) m_tex_y.addElement((float)size);
        m_tex_y.getSelectBoxElements().forEach(elm -> elm.getStyle().setFontSize(20f));
        m_tex_y.setVisibleCount(10); m_tex_y.setElementHeight(20);
        m_tex_y.getSelectionButton().getStyle().setFontSize(20f);
        m_tex_y.addSelectBoxChangeSelectionEventListener(event -> updateModelTexSize(event, false));
		model.getContainer().add(m_tex_s = new SelectBox<>(200, pass, 90, 20));
        m_tex_s.addElement(1f); m_tex_s.addElement(2f); m_tex_s.addElement(3f); m_tex_s.addElement(4f);
        m_tex_s.getSelectBoxElements().forEach(elm -> elm.getStyle().setFontSize(20f));
        m_tex_s.setVisibleCount(10); m_tex_s.setElementHeight(20);
        m_tex_s.getSelectionButton().getStyle().setFontSize(20f);
        m_tex_s.addSelectBoxChangeSelectionEventListener(event -> updateModelTexSize(event, null));
        model.getContainer().add(new Label(translate("editor.model_group.model.texture"), 3, pass += 24, 290, 20));
		model.getContainer().add(model_texture = new TextField(FMTB.MODEL.texture, 3, pass += 24, 290, 20));
		model_texture.getListenerMap().addListener(MouseClickEvent.class, listener -> {
			if(listener.getAction() == CLICK){
				if(listener.getButton() == MouseButton.MOUSE_BUTTON_LEFT){
					FileSelector.select(translate("editor.model_group.model.texture.select"), "./", FileSelector.TYPE_PNG, false, file -> {
						if(file == null) return;
						String name = file.getPath(); TextureManager.loadTextureFromFile(name, file);
						FMTB.MODEL.setTexture(name); FMTB.MODEL.updateFields(); 
					});
				}
				else if(listener.getButton() == MouseButton.MOUSE_BUTTON_RIGHT){
					if(FMTB.MODEL.texture != null && TextureManager.getTexture(FMTB.MODEL.texture, true) != null){
						FMTB.MODEL.setTexture(null); TextureManager.removeTexture(FMTB.MODEL.texture);
					} FMTB.MODEL.updateFields(); return;
				}
			}
		});
		model.getContainer().add(new Label(translate("editor.model_group.model.name"), 3, pass += 24, 290, 20));
		model.getContainer().add(model_name = new TextField(FMTB.MODEL.name, 3, pass += 24, 290, 20));
		model_name.addTextInputContentChangeEventListener(listener -> name_cache = UserInterfaceUtils.validateString(listener));
		model_name.getListenerMap().addListener(FocusEvent.class, listener -> {
			if(!listener.isFocused() && !name_cache.equals(FMTB.MODEL.name))
				FMTB.get().setTitle(FMTB.MODEL.name = name_cache); FMTB.MODEL.button.update();
		});
		model.setSize(296, pass + 52);
        this.addSub(model); pass = -20;
        //
		EditorWidget group = new EditorWidget(this, translate("editor.model_group.group"), 0, 0, 0, 0);
		group.getContainer().add(new Label(translate("editor.model_group.group.color"), 3, pass += 24, 290, 20));
		group.getContainer().add(group_color = new ColorField(group.getContainer(), result -> {
			TurboList sel = FMTB.MODEL.getFirstSelectedGroup(); if(sel == null) return;
			if(sel.color == null) sel.color = RGB.WHITE.copy(); sel.color.packed = result;
		}, 3, pass += 24, 290, 20));
		group.getContainer().add(new Label(translate("editor.model_group.group.name"), 3, pass += 24, 290, 20));
		group.getContainer().add(group_name = new TextField(FMTB.NO_POLYGON_SELECTED, 3, pass += 24, 290, 20));
		group_name.addTextInputContentChangeEventListener(listener -> name_cache = UserInterfaceUtils.validateString(listener));
		group_name.getListenerMap().addListener(FocusEvent.class, listener -> {
			if(!listener.isFocused() && !FMTB.MODEL.getSelected().isEmpty()){
				TurboList list = null;
				if(FMTB.MODEL.getDirectlySelectedGroupsAmount() == 1){
					if(FMTB.MODEL.getGroups().isEmpty()) return;
					list = FMTB.MODEL.getFirstSelectedGroup();
					list = FMTB.MODEL.getGroups().remove(list.id);
					list.id = group_name.getTextState().getText().replace(" ", "_").replace("-", "_").replace(".", "");
					while(FMTB.MODEL.getGroups().contains(list.id)){ list.id += "_"; }
					FMTB.MODEL.getGroups().add(list);
				}
				else{
					ArrayList<TurboList> arrlist = FMTB.MODEL.getDirectlySelectedGroups();
					for(int i = 0; i < arrlist.size(); i++){
						list = FMTB.MODEL.getGroups().remove(arrlist.get(i).id); if(list == null) continue;
						list.id = group_name.getTextState().getText().replace(" ", "_").replace("-", "_").replace(".", "");
						list.id += list.id.contains("_") ? "_" + i : i + "";
						while(FMTB.MODEL.getGroups().contains(list.id)){ list.id += "_"; }
						FMTB.MODEL.getGroups().add(list);
					}
				}
				Editors.general.refreshGroups();
				FMTB.MODEL.getSelected().clear();
			}
		});
		group.getContainer().add(new Label(translate("editor.model_group.group.texture_size"), 3, pass += 24, 290, 20));
		group.getContainer().add(g_tex_x = new SelectBox<>(4, pass += 24, 90, 20));
        for(int size : texsizes) g_tex_x.addElement((float)size);
        g_tex_x.getSelectBoxElements().forEach(elm -> elm.getStyle().setFontSize(20f));
        g_tex_x.setVisibleCount(10); g_tex_x.setElementHeight(20);
        g_tex_x.getSelectionButton().getStyle().setFontSize(20f);
        g_tex_x.addSelectBoxChangeSelectionEventListener(event -> updateGroupTexSize(event, true));
        group.getContainer().add(g_tex_y = new SelectBox<>(102, pass, 90, 20));
        for(int size : texsizes) g_tex_y.addElement((float)size);
        g_tex_y.getSelectBoxElements().forEach(elm -> elm.getStyle().setFontSize(20f));
        g_tex_y.setVisibleCount(10); g_tex_y.setElementHeight(20);
        g_tex_y.getSelectionButton().getStyle().setFontSize(20f);
        g_tex_y.addSelectBoxChangeSelectionEventListener(event -> updateGroupTexSize(event, false));
        group.getContainer().add(g_tex_s = new SelectBox<>(200, pass, 90, 20));
        g_tex_s.addElement(1f); g_tex_s.addElement(2f); g_tex_s.addElement(3f); g_tex_s.addElement(4f);
        g_tex_s.getSelectBoxElements().forEach(elm -> elm.getStyle().setFontSize(20f));
        g_tex_s.setVisibleCount(10); g_tex_s.setElementHeight(20);
        g_tex_s.getSelectionButton().getStyle().setFontSize(20f);
        g_tex_s.addSelectBoxChangeSelectionEventListener(event -> updateGroupTexSize(event, null));
        //
        Label exoff_label = new Label(translate("editor.model_group.group.export_offset"), 3, pass += 24, 240, 20);
        Button exoff_autobutton = new Button("AUTO", 240, pass + 2, 50, 18);
        exoff_autobutton.getListenerMap().addListener(MouseClickEvent.class, listener -> {
        	if(listener.getAction() != CLICK) return;
        	boolean opp = listener.getButton() != MouseButton.MOUSE_BUTTON_LEFT;
    		TurboList list = FMTB.MODEL.getFirstSelectedGroup();
    		if(list == null || list.size() < 1) return;
    		if(list.exportoffset == null) list.exportoffset = new Vec3f();
			list.exportoffset.xCoord = opp ? -list.get(0).pos.xCoord : list.get(0).pos.xCoord;
			list.exportoffset.yCoord = opp ? -list.get(0).pos.yCoord : list.get(0).pos.yCoord;
			list.exportoffset.zCoord = opp ? -list.get(0).pos.zCoord : list.get(0).pos.zCoord;
			exoff_x.apply(list.exportoffset.xCoord);
			exoff_y.apply(list.exportoffset.yCoord);
			exoff_z.apply(list.exportoffset.zCoord);
        });
        exoff_autobutton.getStyle().setFontSize(16f);
        Tooltip exoff_buttontooltip = new Tooltip("Copies first polygon's position.\nleft = normal / rightclick = opposite");
		exoff_buttontooltip.setSize(250, 40);
		exoff_buttontooltip.setPosition(-200, -40);
		exoff_buttontooltip.getStyle().getBackground().setColor(FMTB.rgba(69, 137, 196, 0.9f));
		exoff_buttontooltip.getStyle().setHorizontalAlign(HorizontalAlign.CENTER);
		exoff_autobutton.setTooltip(exoff_buttontooltip);
        group.getContainer().add(exoff_autobutton);
		group.getContainer().add(exoff_label);
        Tooltip exoff_tooltip = new Tooltip("This is valid for FVTM and Flansmod* Export. (e.g. 'translate(group, x, y, z);')");
		exoff_tooltip.setSize(290, 40);
		exoff_tooltip.setPosition(0, -40);
		exoff_tooltip.getStyle().getBackground().setColor(FMTB.rgba(69, 137, 196, 0.9f));
		exoff_tooltip.getStyle().setHorizontalAlign(HorizontalAlign.CENTER);
		exoff_label.setTooltip(exoff_tooltip);
		group.getContainer().add(exoff_x = new NumberField(4, pass += 24, 90, 20).setup(Integer.MIN_VALUE, Integer.MAX_VALUE, true, () -> setgroupoffset()));
		group.getContainer().add(exoff_y = new NumberField(102, pass, 90, 20).setup(Integer.MIN_VALUE, Integer.MAX_VALUE, true, () -> setgroupoffset()));
		group.getContainer().add(exoff_z = new NumberField(200, pass, 90, 20).setup(Integer.MIN_VALUE, Integer.MAX_VALUE, true, () -> setgroupoffset()));
		//exoff_x.setTooltip(exoff_tooltip); exoff_y.setTooltip(exoff_tooltip); exoff_z.setTooltip(exoff_tooltip);
		//
		group.getContainer().add(new Label(translate("editor.model_group.group.texture"), 3, pass += 24, 290, 20));
		group.getContainer().add(group_texture = new TextField(FMTB.MODEL.texture, 3, pass += 24, 290, 20));
		group_texture.getListenerMap().addListener(MouseClickEvent.class, listener -> {
			if(listener.getAction() == CLICK){
				if(FMTB.MODEL.getSelected().isEmpty()) return;
				if(listener.getButton() == MouseButton.MOUSE_BUTTON_LEFT){
					ArrayList<TurboList> arrlist = FMTB.MODEL.getDirectlySelectedGroups();
					for(TurboList list : arrlist){
						if(TextureManager.getTexture(list.getGroupTexture(), true) != null){
							FMTB.MODEL.setTexture(null); TextureManager.removeTexture(list.getGroupTexture());
						} list.setTexture(null, 0, 0); list.forEach(mrt -> mrt.recompile());
					} FMTB.MODEL.updateFields();
				}
				else if(listener.getButton() == MouseButton.MOUSE_BUTTON_RIGHT){
					FileSelector.select(translate("editor.model_group.group.texture.select"), "./", FileSelector.TYPE_PNG, false, file -> {
						String name = file.getPath(); TextureManager.loadTextureFromFile(name, file);
						TextureManager.Texture texture = TextureManager.getTexture(name, false);
						ArrayList<TurboList> arrlist = FMTB.MODEL.getDirectlySelectedGroups();
						for(TurboList list : arrlist){
							list.setTexture(name, texture.getWidth(), texture.getHeight());
							list.recompile();
						} FMTB.MODEL.updateFields(); 
					}); return;
				}
			}
		});
		group.getContainer().add(new Label(translate("editor.model_group.group.add_animator"), 3, pass += 24, 290, 20));
        group.getContainer().add(add_anim = new SelectBox<>(3, pass += 24, 290, 20));
        for(Animation am : Animator.nani){ add_anim.addElement(am); }
        add_anim.getSelectBoxElements().forEach(elm -> {
    		Background background = new Background();
    		background.setColor(new Vector4f(0.8f, 0.8f, 0.8f, 1f));
        	elm.getStyle().setFontSize(20f);
        	elm.getStyle().setHorizontalAlign(HorizontalAlign.LEFT);
        	if(elm.getObject().id.startsWith("#") && !elm.getObject().id.endsWith("#")){
        		elm.getStyle().setBackground(background);
        	}
        });
        add_anim.setVisibleCount(12); add_anim.setElementHeight(20);
        add_anim.getSelectionButton().getStyle().setFontSize(20f);
        add_anim.addSelectBoxChangeSelectionEventListener(event -> addAnimation(event));
		group.setSize(296, pass + 52);
        this.addSub(group); pass = -20;
        //
        animations = new AnimationsEditorWidget(this, translate("editor.model_group.animations"), 0, 0, 0, 0);
        animations.refresh(null); this.addSub(animations); pass = -20;
        //
        reOrderWidgets();
	}
	
	private void setgroupoffset(){
		ArrayList<TurboList> arrlist = FMTB.MODEL.getDirectlySelectedGroups();
		if(arrlist.isEmpty()) return;
		float xval = exoff_x.getValue();
		float yval = exoff_y.getValue();
		float zval = exoff_z.getValue();
		if(xval == 0f && yval == 0f && zval == 0f){
			arrlist.forEach(list -> list.exportoffset = null);
		}
		else{
			arrlist.forEach(list -> {
				if(list.exportoffset == null) list.exportoffset = new Vec3f();
				list.exportoffset.xCoord = xval;
				list.exportoffset.yCoord = yval;
				list.exportoffset.zCoord = zval;
			});
		}
	}

	public static class AnimationsEditorWidget extends EditorWidget {
		
		private TurboList group = null;

		public AnimationsEditorWidget(EditorBase base, String title, int x, int y, int w, int h){
			super(base, title, x, y, w, h);
		}

		public void refresh(TurboList list){
			if(group != list) reset(); int pass = -20;
			if(list != null){
				for(int i = 0; i < list.animations.size(); i++){
					Button button = new Button("[" + i + "]" + list.animations.get(i).id, 3, pass += 24, 290, 20);
					final int j = i; button.getListenerMap().addListener(MouseClickEvent.class, listener -> {
						if(listener.getAction() != CLICK) return;
						if(listener.getButton() == MouseButton.MOUSE_BUTTON_LEFT){
							Animation anim = list.animations.get(j); if(anim == null) return; FMTB.MODEL.updateFields();
							SettingsBox.open("[" + anim.id + "] " + translate("editor.model_group.group.animator_settings"), anim.settings.values(), false,
								settings -> { anim.onSettingsUpdate(); FMTB.MODEL.updateFields();});
						}
						else if(listener.getButton() == MouseButton.MOUSE_BUTTON_RIGHT){
							Animation anim = list.animations.get(j);
							anim.button.removeFromSubTree(); list.animations.remove(anim);
							list.abutton.recalculateSize(); FMTB.MODEL.updateFields();
						}
					});
					button.getStyle().setHorizontalAlign(HorizontalAlign.LEFT);
					this.getContainer().add(button);
				}
			}
			this.setSize(296, pass + 52);
		}

		private void reset(){
			this.getContainer().clearChildComponents();
		}
		
	}

	private void addAnimation(SelectBoxChangeSelectionEvent<Animation> event){
		if(FMTB.MODEL.getSelected().isEmpty()) return;
		if(event.getNewValue().id.startsWith("#")) return;
		final Animation ani = event.getNewValue().copy(null);
		ArrayList<TurboList> lists = FMTB.MODEL.getDirectlySelectedGroups();
		SettingsBox.open(translate("editor.model_group.group.animator_settings"), ani.settings.values(), false, settings -> {
			for(TurboList list : lists){
				Animation anim = ani.copy(list); list.animations.add(anim);
				anim.button.setRoot(list.abutton);
			} FMTB.MODEL.updateFields();
		}); 
		return;
	}

	private void updateModelPos(boolean full){
		float x = (full ? pos_x : poss_x).getValue();
		float y = (full ? pos_y : poss_y).getValue();
		float z = (full ? pos_z : poss_z).getValue();
		if(FMTB.MODEL.pos == null) FMTB.MODEL.pos = new Vec3f(0, 0, 0);
		FMTB.MODEL.pos.xCoord = full ? x : x * Static.sixteenth;
		FMTB.MODEL.pos.yCoord = full ? y : y * Static.sixteenth;
		FMTB.MODEL.pos.zCoord = full ? z : z * Static.sixteenth;
	}

	private void updateModelRot(){
		float x = rot_x.getValue(), y = rot_y.getValue(), z = rot_z.getValue();
		if(FMTB.MODEL.rot == null) FMTB.MODEL.rot = new Vec3f(0, 0, 0);
		FMTB.MODEL.rot.xCoord = x; FMTB.MODEL.rot.yCoord = y; FMTB.MODEL.rot.zCoord = z;
	}

	private void updateModelTexSize(SelectBoxChangeSelectionEvent<Float> event, Boolean bool){
		if(FMTB.MODEL == null) return; int value = (int)(event.getNewValue() + 0f);
		if(bool == null) FMTB.MODEL.textureScale = value;
		else if(bool) FMTB.MODEL.textureSizeX = value;
		else FMTB.MODEL.textureSizeY = value;
		TextureUpdate.updateSize(null); return;
	}

	private void updateGroupTexSize(SelectBoxChangeSelectionEvent<Float> event, Boolean bool){
		if(FMTB.MODEL == null) return; int value = (int)(event.getNewValue() + 0f);
		if(FMTB.MODEL.getDirectlySelectedGroupsAmount() == 0) return;
		for(TurboList list : FMTB.MODEL.getDirectlySelectedGroups()){
			if(bool == null) FMTB.MODEL.textureScale = value;
			else if(bool) FMTB.MODEL.textureSizeX = value;
			else FMTB.MODEL.textureSizeY = value;
			TextureUpdate.updateSize(list);
			list.forEach(mrt -> mrt.recompile());
		} return;
	}
	
}
