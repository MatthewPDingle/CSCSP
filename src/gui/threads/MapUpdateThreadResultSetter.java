package gui.threads;

import gui.MapCell;

import java.util.HashMap;

public interface MapUpdateThreadResultSetter {
	public void setResult(HashMap<String, MapCell> result);	
}