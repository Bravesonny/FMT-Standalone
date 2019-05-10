package net.fexcraft.app.fmt.utils;

import java.io.File;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.fexcraft.app.fmt.FMTB;
import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.utils.Print;

public class Settings {
	
	/*private static boolean fullscreen, floor = true, demo, lines = true, cube = true, polygon_marker = true, polygon_count = true, lighting = false, editor_shortcuts = false;//, raypick = false;
	public static RGB selectedColor = new RGB(255, 255, 0);
	public static RGB background_color = new RGB(127, 127, 127);
	static{ background_color.alpha = 0.2f; }
	public static float[] light0_position = new float[]{ 0, 1, 0, 0 };
	private static String language = "default";*/
	private static Setting floor, lines, demo, cube, polygon_marker, polygon_count, lighting;

	public static boolean floor(){ return floor.getValue(); }

	public static boolean lines(){ return lines.getValue(); }

	public static boolean demo(){ return demo.getValue(); }

	public static boolean cube(){ return cube.getValue(); }
	
	public static boolean polygonMarker(){ return polygon_marker.getValue(); }

	public static boolean polygonCount(){ return polygon_count.getValue(); }

	public static boolean lighting(){ return lighting.getValue(); }
	
	//

	public static boolean toggleFloor(){
		return floor.toggle();
	}

	public static boolean toggleLines(){
		return lines.toggle();
	}

	public static boolean toggleCube(){
		return cube.toggle();
	}

	public static boolean togglePolygonMarker(){
		return polygon_marker.toggle();
	}
	
	public static boolean toggleDemo(){
		return demo.toggle();
	}

	public static boolean togglePolygonCount(){
		return polygon_count.toggle();
	}

	public static boolean toggleLighting(){
		Print.console("Toggling lighting: " + !(boolean)lighting.getValue());
		return lighting.toggle();
	}

	public static RGB getSelectedColor(){
		return SETTINGS.get("selection_color").getValue();
	}

	public static float[] getBackGroundColor(){
		return SETTINGS.get("background_color").getValue();
	}

	public static float[] getLight0Position(){
		return SETTINGS.get("light0_position").getValue();
	}
	
	//
	
	public static SettingsMap DEFAULTS = new SettingsMap(), SETTINGS = new SettingsMap();
	static {
		DEFAULTS.add(new Setting(Type.RGB, "selection_color", new RGB(255, 255, 0)));
		DEFAULTS.add(new Setting(Type.FLOAT_ARRAY, "background_color", new float[]{ 0.5f, 0.5f, 0.5f, 0.2f }));
		DEFAULTS.add(new Setting(Type.BOOLEAN, "fullscreen", false));
		DEFAULTS.add(new Setting(Type.BOOLEAN, "floor", true));
		DEFAULTS.add(new Setting(Type.BOOLEAN, "lines", true));
		DEFAULTS.add(new Setting(Type.BOOLEAN, "cube", true));
		DEFAULTS.add(new Setting(Type.BOOLEAN, "demo", false));
		DEFAULTS.add(new Setting(Type.BOOLEAN, "polygon_marker", false));
		DEFAULTS.add(new Setting(Type.BOOLEAN, "polygon_count", true));
		DEFAULTS.add(new Setting(Type.BOOLEAN, "lighting", false));
		DEFAULTS.add(new Setting(Type.FLOAT_ARRAY, "light0_position", new float[]{ 0, 1, 0, 0 }));
		DEFAULTS.add(new Setting(Type.STRING, "language_code", "default"));
	}

	public static void load() throws Throwable {
		JsonObject obj = JsonUtil.get(new File("./settings.json"));
		if(!obj.has("format")){ SETTINGS.putAll(DEFAULTS); }//assume it's the old format
		if(obj.has("settings")){
			obj = obj.get("settings").getAsJsonObject();
			obj.entrySet().forEach(entry -> {
				try{
					JsonObject jsn = entry.getValue().getAsJsonObject();
					String type = jsn.get("type").getAsString();
					SETTINGS.add(new Setting(type, entry.getKey(), jsn.get("value")));
				}
				catch(Exception e){
					e.printStackTrace();
					Print.console("Failed to load setting '" + entry.getKey() + "'!");
				}
			});
		}
		floor = SETTINGS.get("floor");
		lines = SETTINGS.get("lines");
		demo = SETTINGS.get("demo");
		cube = SETTINGS.get("cube");
		polygon_marker = SETTINGS.get("polygon_marker");
		polygon_count = SETTINGS.get("polygon_count");
		lighting = SETTINGS.get("lighting");
	}

	public static void save(){
		JsonObject obj = new JsonObject();
		obj.addProperty("format", 1);
		JsonObject settings = new JsonObject();
		SETTINGS.values().forEach(entry -> {
			JsonObject jsn = new JsonObject();
			jsn.addProperty("type", entry.getType().name().toLowerCase());
			jsn.add("value", entry.save()); settings.add(entry.getId(), jsn);
		});
		obj.add("settings", settings);
		obj.addProperty("last_fmt_version_used", FMTB.version);
		obj.addProperty("last_fmt_exit", Time.getAsString(Time.getDate()));
		JsonUtil.write(new File("./settings.json"), obj);
	}
	
	/** see https://stackoverflow.com/a/3758880/6095495 */
	public static final String byteCountToString(long bytes, boolean si){
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static String getLanguage(){
		return SETTINGS.get("language_code").getValue();
	}

	public static SettingsMap getMap(){
		return SETTINGS;
	}
	
	@SuppressWarnings("serial")
	public static class SettingsMap extends TreeMap<String, Setting> {
		
		public void add(Setting setting){
			this.put(setting.getId(), setting);
		}

		public Setting getAt(int i){
			return this.values().toArray(new Setting[0])[i];
		}
		
	}

	@SuppressWarnings("unchecked")
	public static class Setting {
		
		private String id;
		private Type type;
		private Object value;
		
		/** For creating defaults or in-code settings. */
		public Setting(Type type, String id, Object value){
			this.type = type; this.id = id; this.value = value;
		}

		/** For parsing of Settings. */
		public Setting(String type, String id, JsonElement elm){
			this.type = Type.valueOf(type.toUpperCase()); this.id = id;
			switch(this.type){
				case BOOLEAN: value = elm.getAsBoolean(); break;
				case FLOAT: value = elm.getAsFloat(); break;
				case FLOAT_ARRAY:{
					JsonArray array = elm.getAsJsonArray();
					float[] arr = new float[array.size()];
					for(int i = 0; i < arr.length; i++)
						arr[i] = array.get(i).getAsFloat();
					value = arr; break;
				}
				case INTEGER: value = elm.getAsInt(); break;
				case RGB:{
					if(elm.isJsonPrimitive()){
						value = new RGB(elm.getAsString());
					}
					else{
						JsonArray array = elm.getAsJsonArray();
						value = new RGB(array.get(0).getAsInt(), array.get(1).getAsInt(),
							array.get(2).getAsInt(),array.size() >= 4 ? array.get(3).getAsFloat() : 1f);
					}
					break;
				}
				case STRING: value = elm.getAsString(); break;
				default: value = elm; break;
			}
		}
		
		/** Don't use unless required. */
		public void setValue(Object newval){
			this.value = newval;
		}
		
		public <U> U getValue(){
			return (U)value;
		}
		
		public boolean toggle(){
			if(value instanceof Boolean){
				return (boolean)(value = !(boolean)value);
			} else return false;
		}
		
		public String getId(){
			return id;
		}
		
		public Type getType(){
			return type;
		}
		
		public boolean validateAndApply(String newval){
			switch(type){
				case BOOLEAN:{
					value = Boolean.parseBoolean(newval);
					return true;
				}
				case FLOAT:{
					try{
						value = Float.parseFloat(newval);
						return true;
					}
					catch(Exception e){
						e.printStackTrace(); return false;
					}
				}
				case FLOAT_ARRAY:
					try{
						String[] arr = newval.split(",");
						float[] all = (float[])value;
						for(int i = 0; i < arr.length; i++){
							if(i >= all.length) break;
							all[i] = Float.parseFloat(arr[i]);
						} return true;
					}
					catch(Exception e){
						e.printStackTrace(); return false;
					}
				case INTEGER:
					try{
						value = Integer.parseInt(newval);
						return true;
					}
					catch(Exception e){
						e.printStackTrace(); return false;
					}
				case RGB:{
					try{
						int i = Integer.parseInt(newval.replace("#", ""), 16);
						((RGB)value).packed = i; return true;
					}
					catch(Exception e){
						e.printStackTrace(); return false;
					}
				}
				case STRING: value = newval;
				default: Print.console("Error - typeless setting.");
			}
			return true;
		}
		
		public String toString(){
			switch(type){
				case BOOLEAN: return value + "";
				case FLOAT: return value + "";
				case FLOAT_ARRAY:{
					float[] arr = (float[])value; String str = "";
					for(int i = 0; i < arr.length; i++){
						str += arr[i]; if(i < arr.length - 1) str += ", ";
					} return str;
				}
				case INTEGER: return value + "";
				case RGB: return "#" + ((RGB)value).toString();
				case STRING: return value + "";
				default: return "[" + value + "]";
			}
		}
		
		public JsonElement save(){
			switch(type){
				case BOOLEAN: return new JsonPrimitive((boolean)value);
				case FLOAT: return new JsonPrimitive((float)value);
				case FLOAT_ARRAY:{
					JsonArray array = new JsonArray();
					float[] arr = (float[])value;
					for(int i = 0; i < arr.length; i++){
						array.add(arr[i]);
					} return array;
				}
				case INTEGER: return new JsonPrimitive((int)value);
				case RGB: return new JsonPrimitive("#" + Integer.toHexString(((RGB)value).packed));
				case STRING: return new JsonPrimitive((String)value);
				default: break;
			}
			return new JsonPrimitive("null");
		}

		public boolean getBooleanValue(){
			if(this.getType().isBoolean()) return (boolean)value; return false;
		}
		
	}
	
	public static enum Type {
		
		STRING, BOOLEAN, INTEGER, FLOAT, RGB, FLOAT_ARRAY;

		public boolean isBoolean(){
			return this == BOOLEAN;
		}
		
	}

}
