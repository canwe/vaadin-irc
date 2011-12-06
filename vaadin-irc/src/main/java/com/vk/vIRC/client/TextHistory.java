package com.vk.vIRC.client;

import java.util.ArrayList;

public class TextHistory {

	ArrayList<StringBuffer> buffer;
	int bufSize = 100;
	int pointer = -1;
	
	public TextHistory(int bufferSize){
		if (bufferSize > 0){
			this.bufSize = bufferSize;
		}
		buffer = new ArrayList<StringBuffer>(bufferSize);
		buffer.add(0, new StringBuffer(""));
	}
	
	public void addText(String s){
		if (s == null || s.equals("")) return;
		pointer = -1;
		buffer.add(0, new StringBuffer(s));
		if (buffer.size() > bufSize) {
			buffer.remove(buffer.size() - 1);
		}
	}
	
	public String getText(int index){
		if (index <0 || index > buffer.size()) return null;
		else return buffer.get(index).toString();
	}
	
	public String getPreviousText(){
		pointer++;
		if (pointer >= buffer.size()) pointer--;
		StringBuffer toSend = buffer.get(pointer);
		return toSend.toString();
	}
	
	public String getNextText(){
		pointer--;
		StringBuffer toSend = null;
		if (pointer < 0) {
			pointer = -1;
			toSend = new StringBuffer("");
		} else {
			toSend = buffer.get(pointer);			
		}
		
		return toSend.toString();
	}
	
	
}
