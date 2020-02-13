package net.fexcraft.app.fmt.ui;

import static org.liquidengine.legui.event.MouseClickEvent.MouseClickAction.CLICK;

import java.util.ArrayList;

import org.liquidengine.legui.component.*;
import org.liquidengine.legui.component.event.slider.SliderChangeValueEventListener;
import org.liquidengine.legui.component.misc.listener.scrollablepanel.ScrollablePanelViewportScrollListener;
import org.liquidengine.legui.component.optional.align.HorizontalAlign;
import org.liquidengine.legui.event.FocusEvent;
import org.liquidengine.legui.event.KeyEvent;
import org.liquidengine.legui.event.MouseClickEvent;
import org.liquidengine.legui.event.ScrollEvent;
import org.liquidengine.legui.event.WindowSizeEvent;
import org.liquidengine.legui.listener.FocusEventListener;
import org.liquidengine.legui.listener.KeyEventListener;
import org.liquidengine.legui.style.Background;
import org.liquidengine.legui.style.Style.DisplayType;
import org.liquidengine.legui.style.color.ColorConstants;
import org.lwjgl.glfw.GLFW;

import net.fexcraft.app.fmt.FMTB;
import net.fexcraft.app.fmt.ui.UserInterpanels.BoolButton;
import net.fexcraft.app.fmt.ui.UserInterpanels.Button20;
import net.fexcraft.app.fmt.ui.UserInterpanels.Dialog20;
import net.fexcraft.app.fmt.ui.UserInterpanels.Label20;
import net.fexcraft.app.fmt.ui.UserInterpanels.NumberInput20;
import net.fexcraft.app.fmt.ui.UserInterpanels.TextInput20;
import net.fexcraft.app.fmt.utils.Translator;
import net.fexcraft.app.fmt.wrappers.PolygonWrapper;
import net.fexcraft.app.fmt.wrappers.ShapeType;
import net.fexcraft.app.fmt.wrappers.TurboList;
import net.fexcraft.lib.common.Static;

public class Editors {
	
	public static GeneralEditor general;
	public static ModelGroupEditor modelgroup;
	//
	public static final ArrayList<EditorBase> editors = new ArrayList<>();

	public static void initializeEditors(Frame frame){
		frame.getContainer().add(general = new GeneralEditor());
		frame.getContainer().add(modelgroup = new ModelGroupEditor());
		//temporary
		//general.show();
	}
	
	public static void hideAll(){
		for(EditorBase editor : editors) editor.hide();
	}
	
	public static void show(String type){
		hideAll();
		switch(type){
			case "general": general.show(); break;
			case "model": case "group":
			case "modelgroup": modelgroup.show(); break;
		}
	}
	
	public static boolean anyVisible(){
		for(EditorBase editor : editors) if(editor.isVisible()) return true; return false;
	}
	
	public static EditorBase getVisible(){
		for(EditorBase editor : editors) if(editor.isVisible()) return editor; return null;
	}
	
	public static class EditorBase extends Panel {

		protected ArrayList<EditorWidget> widgets = new ArrayList<>();
		protected ScrollablePanel scrollable;
		
		public EditorBase(){
			super(0, 30, 304, FMTB.HEIGHT - 30); editors.add(this);
			this.getListenerMap().addListener(WindowSizeEvent.class, event -> {
				this.setSize(304, event.getHeight() - 30);
				scrollable.setSize(304, event.getHeight() - 80);
				scrollable.getContainer().setSize(296, event.getHeight() - 88);
			});
			String[] arr = new String[]{ "normal", "sixteenth", "decimal"}; int off = 0;
			Label label = new Label(translate("editor.multiplicator"), 4, 4, 100, 24);
			super.add(label); label.getTextState().setFontSize(20); int am = 0;
			Label current = new Label(format("editor.multiplicator.current", 1f), 4, 28, 100, 24);
			super.add(current); current.getTextState().setFontSize(20);
			for(String string : arr){
				Slider multislider = new Slider(148, 4 + off, 150, 14);
				switch(string){
					case "normal":{
						multislider.setMinValue(0); multislider.setMaxValue(64);
						multislider.setStepSize(1); multislider.setValue(1f); am = 0;
						break;
					}
					case "sixteenth":{
						multislider.setMinValue(0); multislider.setMaxValue(1); am = 4;
						multislider.setStepSize(Static.sixteenth); multislider.setValue(1f);
						break;
					}
					case "decimal":{
						multislider.setMinValue(0); multislider.setMaxValue(1); am = 1;
						multislider.setStepSize(0.1f); multislider.setValue(1f);
						break;
					}
				}
		        final Tooltip multitip = new Tooltip();
		        multitip.setSize(100, 20); multitip.getTextState().setFontSize(20);
		        multitip.setPosition(multislider.getSize().x + 2, 0); final String amo = "%." + am + "f";
		        multitip.getTextState().setText(translate("editor.multiplicator.value") + String.format(amo, multislider.getValue()));
		        multislider.addSliderChangeValueEventListener((SliderChangeValueEventListener) event -> {
		            multitip.getTextState().setText(translate("editor.multiplicator.value") + String.format(amo, event.getNewValue()));
		            current.getTextState().setText(format("editor.multiplicator.current", event.getNewValue()));
		            multitip.setSize(100, 28); FMTB.MODEL.rate = event.getNewValue();
		        });
		        multislider.setTooltip(multitip);
		        super.add(multislider); off += 16;
			}
	        scrollable = new ScrollablePanel(0, 54, 304, FMTB.HEIGHT - 80);
	        scrollable.getStyle().getBackground().setColor(1, 1, 1, 1);
	        scrollable.setHorizontalScrollBarVisible(false);
	        scrollable.getContainer().setSize(296, FMTB.HEIGHT - 80);
	        scrollable.getViewport().getListenerMap().removeAllListeners(ScrollEvent.class);
	        scrollable.getViewport().getListenerMap().addListener(ScrollEvent.class, new SPVSL());
	        super.add(scrollable); this.hide();
		}

		public void toggle(){
			if(isVisible()) hide(); else show();
		}
		
		public void hide(){
			this.getStyle().setDisplay(DisplayType.NONE);
		}
		
		public void show(){
			this.getStyle().setDisplay(DisplayType.MANUAL);
		}
		
		public boolean addSub(Component com){
			if(com instanceof EditorWidget) widgets.add((EditorWidget)com);
			return scrollable.getContainer().add(com);
		}

		protected void reOrderWidgets(){
			float size = 0; for(EditorWidget widget : widgets) size += widget.getSize().y + 2;
			scrollable.getContainer().setSize(scrollable.getSize().x, size > FMTB.HEIGHT - 80 ? size : FMTB.HEIGHT - 80); size = 0;
			for(EditorWidget widget : widgets){ widget.setPosition(0, size); size += widget.getSize().y + 2; }
		}
		
	}
	
	public static class SPVSL extends ScrollablePanelViewportScrollListener {
		
	    @Override
	    public void process(@SuppressWarnings("rawtypes") ScrollEvent event){
	    	if(FMTB.field_scrolled) return; else super.process(event);
	    }
	    
	}
	
	public static class GeneralEditor extends EditorBase {
		
		public static TextInput polygon_name;
		public static NumberInput20 size_x, size_y, size_z;
		public static NumberInput20 pos_x, pos_y, pos_z;
		public static NumberInput20 off_x, off_y, off_z;
		public static NumberInput20 rot_x, rot_y, rot_z;
		public static NumberInput20 texture_x, texture_y;
		public static NumberInput20 cyl0_x, cyl0_y, cyl0_z;
		public static NumberInput20 cyl1_x, cyl1_y, cyl1_z;
		public static NumberInput20 cyl2_x, cyl2_y;
		public static NumberInput20 cyl3_x, cyl3_y, cyl3_z;
		public static BoolButton cyl4_x, cyl4_y, cyl5_x, cyl5_y, cyl6_x;
		public static NumberInput20 cyl6_y, cyl6_z;
		public static NumberInput20 cyl7_x, cyl7_y, cyl7_z;
		public static NumberInput20[] corner_x, corner_y, corner_z;
		public static NumberInput20[][] texrect_a = new NumberInput20[12][4], texrect_b = new NumberInput20[6][4];
		public static NumberInput20 marker_color, marker_scale, marker_angle;
		public static BoolButton marker_biped;
		public static SelectBox<Object> polygon_group, polygon_type;
		
		@SuppressWarnings("unchecked")
		public GeneralEditor(){
			super(); int pass = -20;
			EditorWidget attributes = new EditorWidget(this, translate("editor.general.attributes"), 0, 0, 0, 0);
	        attributes.getContainer().add(new Label20(translate("editor.general.attributes.group"), 3, pass += 24, 290, 20));
	        attributes.getContainer().add(polygon_group = new SelectBox<>(3, pass += 24, 290, 20));
	        polygon_group.addElement("> new_group <"); polygon_group.getSelectBoxElements().get(0).getTextState().setFontSize(20f);
	        polygon_group.setVisibleCount(12); polygon_group.setElementHeight(20);
			polygon_group.getSelectionButton().getTextState().setFontSize(20f);
	        polygon_group.addSelectBoxChangeSelectionEventListener(event -> {
	        	if(event.getNewValue().toString().equals("> new_group <")){
		            Dialog20 dialog = new Dialog20(translate("editor.general.attributes.new_group.title"), 300, 120);
		            Label20 label = new Label20(translate("editor.general.attributes.new_group.desc"), 10, 10, 280, 20);
		            TextInput20 input = new TextInput20("new_group", 10, 40, 280, 20);
		            Button20 confirm = new Button20(translate("editor.general.attributes.new_group.confirm"), 10, 70, 70, 20);
	                Button20 cancel = new Button20(translate("editor.general.attributes.new_group.cancel"), 90, 70, 70, 20);
		            dialog.getContainer().add(input); dialog.getContainer().add(label);
		            dialog.getContainer().add(confirm); dialog.getContainer().add(cancel);
	                confirm.getListenerMap().addListener(MouseClickEvent.class, e -> {
	                    if(CLICK == e.getAction()){
	                    	String text = input.getTextState().getText();
	                    	if(text.equals("new_group")) text += "0";
	                    	while(FMTB.MODEL.getGroups().contains(text)) text += "_";
	                    	FMTB.MODEL.getGroups().add(new TurboList(text));
							FMTB.MODEL.changeGroupOfSelected(FMTB.MODEL.getSelected(), text);
	                        dialog.close();
	                    }
	                });
	                cancel.getListenerMap().addListener(MouseClickEvent.class, e -> { if(CLICK == e.getAction()) dialog.close(); });
		            dialog.setResizable(false); dialog.show(event.getFrame());
	        	}
	        	else{
	        		FMTB.MODEL.changeGroupOfSelected(FMTB.MODEL.getSelected(), event.getNewValue().toString());
	        	}
	        	polygon_group.setSelected(0, false);
	        });
	        attributes.getContainer().add(new Label20(translate("editor.general.attributes.name"), 3, pass += 24, 290, 20));
	        attributes.getContainer().add(polygon_name = new TextInput20(translate("error.no_polygon_selected"), 3, pass += 24, 290, 20));
	        polygon_name.addTextInputContentChangeEventListener(event -> {
				String validated = UserInterpanels.validateString(event);
				if(FMTB.MODEL.getSelected().isEmpty()) return; PolygonWrapper wrapper;
				if(FMTB.MODEL.getSelected().size() == 1){
					wrapper = FMTB.MODEL.getFirstSelection();
					if(wrapper != null) wrapper.name = validated;
				}
				else{
					ArrayList<PolygonWrapper> polis = FMTB.MODEL.getSelected();
					for(int i = 0; i < polis.size(); i++){
						wrapper = polis.get(i);
						if(wrapper != null){
							String str = validated.contains("_") ? "_" + i : validated.contains("-") ? "-" + i :
								validated.contains(" ") ? " " + i : validated.contains(".") ? "." + i : i + "";
							wrapper.name = validated + str;
						}
					}
				}
	        });
	        attributes.getContainer().add(new Label20(translate("editor.general.attributes.type"), 3, pass += 24, 290, 20));
	        attributes.getContainer().add(polygon_type = new SelectBox<>(3, pass += 24, 290, 20));
	        for(ShapeType type : ShapeType.getSupportedValues()) polygon_type.addElement(type.name().toLowerCase());
	        polygon_type.getSelectBoxElements().forEach(elm -> elm.getTextState().setFontSize(20f));
	        polygon_type.setVisibleCount(12); polygon_type.setElementHeight(20);
	        polygon_type.getSelectionButton().getTextState().setFontSize(20f);
	        polygon_type.addSelectBoxChangeSelectionEventListener(event -> {
	        	FMTB.MODEL.changeTypeOfSelected(FMTB.MODEL.getSelected(), event.getNewValue().toString());
	        });
	        Button20 painttotex = new Button20(translate("editor.general.attributes.painttotexture"), 3, 8 + (pass += 24), 290, 20);
	        painttotex.getListenerMap().addListener(MouseClickEvent.class, UserInterpanels.NOT_REIMPLEMENTED_YET/*event -> {
				if(FMTB.MODEL.texture == null){
					String str = translate("dialog.editor.general.attributes.burntotex.notex", "There is no texture loaded.");
					String ok = translate("dialog.editor.general.attributes.burntotex.notex.confirm", "ok");
					FMTB.showDialogbox(str, ok, translate("dialog.editor.general.attributes.burntotex.notex.cancel", "load"), DialogBox.NOTHING, () -> {
						try{
							FMTB.get().UI.getElement("toolbar").getElement("textures").getElement("menu").getElement("select").onButtonClick(x, y, left, true);
						}
						catch(Exception e){
							e.printStackTrace();
						}
					});
				}
				else{
					ArrayList<PolygonWrapper> selection = FMTB.MODEL.getSelected();
					for(PolygonWrapper poly : selection){
						String texname = poly.getTurboList().getGroupTexture() == null ? FMTB.MODEL.texture : poly.getTurboList().getGroupTexture();
						Texture tex = TextureManager.getTexture(texname, true);
						if(tex == null){//TODO group tex compensation
							String str = translate("dialog.editor.general.attributes.burntotex.tex_not_found", "Texture not found in Memory.<nl>This rather bad.");
							FMTB.showDialogbox(str, translate("dialog.editor.general.attributes.burntotex.tex_not_found.confirm", "ok"), null, DialogBox.NOTHING, null);
							return true;
						}
						poly.burnToTexture(tex.getImage(), null); poly.recompile(); TextureManager.saveTexture(texname); tex.rebind();
						Print.console("Polygon painted into Texture.");
					}
					return true;
				}
	        }*/);//TODO
	        attributes.getContainer().add(painttotex);
	        attributes.setSize(296, pass + 52 + 4);
	        this.addSub(attributes); pass = -20;
	        //
			EditorWidget shape = new EditorWidget(this, translate("editor.general.shape"), 0, 0, 0, 0);
			shape.getContainer().add(new Label20(translate("editor.general.shape.size"), 3, pass += 24, 290, 20));
			shape.getContainer().add(size_x = new NumberInput20(4, pass += 24, 90, 20).setup("sizex", 0, Integer.MAX_VALUE, false));
			shape.getContainer().add(size_y = new NumberInput20(102, pass, 90, 20).setup("sizey", 0, Integer.MAX_VALUE, false));
			shape.getContainer().add(size_z = new NumberInput20(200, pass, 90, 20).setup("sizez", 0, Integer.MAX_VALUE, false));
			shape.getContainer().add(new Label20(translate("editor.general.shape.position"), 3, pass += 24, 290, 20));
			shape.getContainer().add(pos_x = new NumberInput20(4, pass += 24, 90, 20).setup("posx", Integer.MIN_VALUE, Integer.MAX_VALUE, true));
			shape.getContainer().add(pos_y = new NumberInput20(102, pass, 90, 20).setup("posy", Integer.MIN_VALUE, Integer.MAX_VALUE, true));
			shape.getContainer().add(pos_z = new NumberInput20(200, pass, 90, 20).setup("posz", Integer.MIN_VALUE, Integer.MAX_VALUE, true));
			shape.getContainer().add(new Label20(translate("editor.general.shape.offset"), 3, pass += 24, 290, 20));
			shape.getContainer().add(off_x = new NumberInput20(4, pass += 24, 90, 20).setup("offx", Integer.MIN_VALUE, Integer.MAX_VALUE, true));
			shape.getContainer().add(off_y = new NumberInput20(102, pass, 90, 20).setup("offy", Integer.MIN_VALUE, Integer.MAX_VALUE, true));
			shape.getContainer().add(off_z = new NumberInput20(200, pass, 90, 20).setup("offz", Integer.MIN_VALUE, Integer.MAX_VALUE, true));
			shape.getContainer().add(new Label20(translate("editor.general.shape.rotation"), 3, pass += 24, 290, 20));
			shape.getContainer().add(rot_x = new NumberInput20(4, pass += 24, 90, 20).setup("rotx", -360, 360, true));
			shape.getContainer().add(rot_y = new NumberInput20(102, pass, 90, 20).setup("roty", -360, 360, true));
			shape.getContainer().add(rot_z = new NumberInput20(200, pass, 90, 20).setup("rotz", -360, 360, true));
			shape.getContainer().add(new Label20(translate("editor.general.shape.texture"), 3, pass += 24, 290, 20));
			shape.getContainer().add(texture_x = new NumberInput20(4, pass += 24, 90, 20).setup("texx", 0, 8192, true));
			shape.getContainer().add(texture_y = new NumberInput20(102, pass, 90, 20).setup("texy", 0, 8192, true));
			shape.setSize(296, pass + 52);
	        this.addSub(shape); pass = -20;
	        //
			EditorWidget shapebox = new EditorWidget(this, translate("editor.general.shapebox"), 0, 0, 0, 0);
			corner_x = new NumberInput20[8]; corner_y = new NumberInput20[8]; corner_z = new NumberInput20[8];
	        for(int i = 0; i < 8; i++){
	        	shapebox.getContainer().add(new Label20(translate("editor.general.shapebox.corner" + i), 3, pass += 24, 290, 20));
				shapebox.getContainer().add(corner_x[i] = new NumberInput20(4, pass += 24, 90, 20).setup("cor" + i + "x", Integer.MIN_VALUE, Integer.MAX_VALUE, true));
				shapebox.getContainer().add(corner_y[i] = new NumberInput20(102, pass, 90, 20).setup("cor" + i + "y", Integer.MIN_VALUE, Integer.MAX_VALUE, true));
				shapebox.getContainer().add(corner_z[i] = new NumberInput20(200, pass, 90, 20).setup("cor" + i + "z", Integer.MIN_VALUE, Integer.MAX_VALUE, true));
	        }
			shapebox.setSize(296, pass + 52);
	        this.addSub(shapebox); pass = -20;
	        //
			EditorWidget cylinder = new EditorWidget(this, translate("editor.general.cylinder"), 0, 0, 0, 0);
			cylinder.getContainer().add(new Label20(translate("editor.general.cylinder.radius_length"), 3, pass += 24, 290, 20));
			cylinder.getContainer().add(cyl0_x = new NumberInput20(4, pass += 24, 90, 20).setup("cyl0x", 1, Integer.MAX_VALUE, false));
			cylinder.getContainer().add(cyl0_y = new NumberInput20(102, pass, 90, 20).setup("cyl0y", 1, Integer.MAX_VALUE, false));
			cylinder.getContainer().add(cyl0_z = new NumberInput20(200, pass, 90, 20).setup("cyl0z", 0, Integer.MAX_VALUE, true));
			cylinder.getContainer().add(new Label20(translate("editor.general.cylinder.segments_direction"), 3, pass += 24, 290, 20));
			cylinder.getContainer().add(cyl1_x = new NumberInput20(4, pass += 24, 90, 20).setup("cyl1x", 3, Integer.MAX_VALUE, false));
			cylinder.getContainer().add(cyl1_y = new NumberInput20(102, pass, 90, 20).setup("cyl1y", 0, 5, false));
			cylinder.getContainer().add(cyl1_z = new NumberInput20(200, pass, 90, 20).setup("cyl1z", 0, Integer.MAX_VALUE, false));
			cylinder.getContainer().add(new Label20(translate("editor.general.cylinder.scale"), 3, pass += 24, 290, 20));
			cylinder.getContainer().add(cyl2_x = new NumberInput20(4, pass += 24, 90, 20).setup("cyl2x", 0, Integer.MAX_VALUE, true));
			cylinder.getContainer().add(cyl2_y = new NumberInput20(102, pass, 90, 20).setup("cyl2y", 0, Integer.MAX_VALUE, true));
			cylinder.getContainer().add(new Label20(translate("editor.general.cylinder.top_offset"), 3, pass += 24, 290, 20));
			cylinder.getContainer().add(cyl3_x = new NumberInput20(4, pass += 24, 90, 20).setup("cyl0x", 1, Integer.MAX_VALUE, true));
			cylinder.getContainer().add(cyl3_y = new NumberInput20(102, pass, 90, 20).setup("cyl3y", 1, Integer.MAX_VALUE, true));
			cylinder.getContainer().add(cyl3_z = new NumberInput20(200, pass, 90, 20).setup("cyl3z", 0, Integer.MAX_VALUE, true));
			cylinder.getContainer().add(new Label20(translate("editor.general.cylinder.top_rotation"), 3, pass += 24, 290, 20));
			cylinder.getContainer().add(cyl7_x = new NumberInput20(4, pass += 24, 90, 20).setup("cyl7x", -360, 360, true));
			cylinder.getContainer().add(cyl7_y = new NumberInput20(102, pass, 90, 20).setup("cyl7y", -360, 360, true));
			cylinder.getContainer().add(cyl7_z = new NumberInput20(200, pass, 90, 20).setup("cyl7z", -360, 360, true));
			cylinder.getContainer().add(new Label20(translate("editor.general.cylinder.visibility_toggle"), 3, pass += 24, 290, 20));
			cylinder.getContainer().add(cyl4_x = new BoolButton("cyl4x", 6, pass += 24, 66, 20));
			cylinder.getContainer().add(cyl4_y = new BoolButton("cyl4y", 78, pass, 66, 20));
			cylinder.getContainer().add(cyl5_x = new BoolButton("cyl5x", 148, pass, 66, 20));
			cylinder.getContainer().add(cyl5_y = new BoolButton("cyl5y", 220, pass, 66, 20));
			cylinder.getContainer().add(new Label20(translate("editor.general.cylinder.radial_texture"), 3, pass += 24, 290, 20));
			cylinder.getContainer().add(cyl6_x = new BoolButton("cyl6x", 4, pass += 24, 90, 20));
			cylinder.getContainer().add(cyl6_y = new NumberInput20(102, pass, 90, 20).setup("cyl6y", 0, Integer.MAX_VALUE, true));
			cylinder.getContainer().add(cyl6_z = new NumberInput20(200, pass, 90, 20).setup("cyl6z", 0, Integer.MAX_VALUE, true));
			cylinder.setSize(296, pass + 52);
	        this.addSub(cylinder); pass = -20;
	        //
			EditorWidget marker = new EditorWidget(this, translate("editor.general.marker"), 0, 0, 0, 0);
			marker.getContainer().add(new Label20(translate("editor.general.marker.color"), 3, pass += 24, 290, 20));
			marker.getContainer().add(marker_color = new NumberInput20(3, pass += 24, 290, 20){
				@Override
				public float getValue(){
					return Integer.parseInt(marker_color.getTextState().getText().replace("#", "").replace("0x", ""), 16);
				}
			});
			marker_color.getListenerMap().addListener(FocusEvent.class, (FocusEventListener)listener -> {
				if(!listener.isFocused()){
					FMTB.MODEL.updateValue(marker_color, "marker_colorx");
				}
			});
			marker_color.getListenerMap().addListener(KeyEvent.class, (KeyEventListener)listener -> {
				if(listener.getKey() == GLFW.GLFW_KEY_ENTER){
					FMTB.MODEL.updateValue(marker_color, "marker_colorx");
				}
			});
	        marker.getContainer().add(new Label20(translate("editor.general.marker.biped_display"), 3, pass += 24, 290, 20));
	        marker.getContainer().add(marker_biped = new BoolButton("marker_bipedx", 4, pass += 24, 90, 20));
	        marker.getContainer().add(marker_angle = new NumberInput20(102, pass, 90, 20).setup("marker_anglex", -360, 360, true));
	        marker.getContainer().add(marker_scale = new NumberInput20(200, pass, 90, 20).setup("marker_scalex", 0, 1024f, true));
			marker.setSize(296, pass + 52);
	        this.addSub(marker); pass = -20;
	        //
			final String[] faces = new String[]{
				translate("editor.general.texrect.front"), translate("editor.general.texrect.back"),
				translate("editor.general.texrect.up"), translate("editor.general.texrect.down"),
				translate("editor.general.texrect.right"), translate("editor.general.texrect.left")
			};
			EditorWidget texrectA = new EditorWidget(this, translate("editor.general.texrect_a"), 0, 0, 0, 0);
			int[] tra = new int[24]; for(int i = 0; i < 12; i++){ tra[i * 2] = 1; tra[i * 2 + 1] = 4; }
			for(int r = 0; r < 12; r++){
				texrectA.getContainer().add(new Label20(format("editor.general.texrect_a.face_" + (r % 2 == 0 ? "x" : "y"), faces[r / 2]), 3, pass += 24, 290, 20));
				for(int i = 0; i < 4; i++){
					String id = "texpos" + (r / 2) + ":" + ((i * 2) + (r % 2 == 1 ? 1 : 0)) + (r % 2 == 0 ? "x" : "y"); if(i == 0) pass += 24;
					texrectA.getContainer().add(texrect_a[r][i] = new NumberInput20(6 + (i * 72), pass, 66, 20).setup(id, 0, Integer.MAX_VALUE, true));
				}
			}
			texrectA.setSize(296, pass + 52);
	        this.addSub(texrectA); pass = -20;
			EditorWidget texrectB = new EditorWidget(this, translate("editor.general.texrect_b"), 0, 0, 0, 0);
			for(int r = 0; r < 6; r++){
				texrectB.getContainer().add(new Label20(format("editor.general.texrect_a.face_" + (r % 2 == 0 ? "x" : "y"), faces[r]), 3, pass += 24, 290, 20));
				for(int i = 0; i < 4; i++){
					String id = "texpos" + r + (i < 2 ? "s" : "e") + (i % 2 == 0 ? "x" : "y"); if(i == 0) pass += 24;
					texrectB.getContainer().add(texrect_a[r][i] = new NumberInput20(6 + (i * 72), pass, 66, 20).setup(id, 0, Integer.MAX_VALUE, true));
				}
			}
			texrectB.setSize(296, pass + 52);
	        this.addSub(texrectB); pass = -20;
			//
	        //reOrderWidgets();
	        texrectA.setMinimized(true);
	        texrectB.setMinimized(true);
		}
		
		public void refreshGroups(){
			while(!polygon_group.getElements().isEmpty()) polygon_group.removeElement(0);
			for(TurboList list : FMTB.MODEL.getGroups()) polygon_group.addElement(list.id);
			polygon_group.addElement("> new_group <");
			polygon_group.getSelectBoxElements().forEach(elm -> elm.getTextState().setFontSize(20f));
		}
		
	}
	
	public static class ModelGroupEditor extends EditorBase {
		
		public ModelGroupEditor(){
			super();
		}
		
	}
	
	public static class EditorWidget extends Widget {
		
		private EditorBase editor;

		public EditorWidget(EditorBase base, String title, int x, int y, int w, int h){
			super(x, y, w, h); editor = base;
			Background background = new Background(); background.setColor(ColorConstants.lightGray());
			getTitleTextState().setFontSize(22); getTitleTextState().setText(title);
			getTitleContainer().getStyle().setBackground(background);
			getTitleContainer().setSize(getTitleContainer().getSize().x, 20);
			getTitleTextState().setHorizontalAlign(HorizontalAlign.CENTER);
	        setCloseable(false); setResizable(false); setDraggable(false);
		}
		
		@Override
		public void setMinimized(boolean bool){
			super.setMinimized(bool);
			editor.reOrderWidgets();
		}

		public void toggle(){
			setMinimized(!isMinimized());
		}
		
	}
	
	public static String translate(String str){
		return Translator.translate(str, "no.lang");
	}
	
	public static String format(String str, Object... objs){
		return Translator.format(str, "no.lang.%s", objs);
	}

	public static void toggleWidget(int i){
		if(i < 0) return;
		if(anyVisible()){
			EditorBase editor = getVisible();
			if(i >= editor.widgets.size()) return;
			editor.widgets.get(i).toggle();
		}
		else{
			if(i >= editors.size()) return;
			hideAll(); editors.get(i).show();
		}
	}

}