package com.vk.vIRC.util;

import java.util.ArrayList;

import org.schwering.irc.lib.IRCConstants;

import com.vaadin.ui.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringParser {

    /** log. */
	private static final Logger log = LoggerFactory.getLogger(StringParser.class);

	public static String warningText(String text){
		text = "<font color = \"red\">" + text + " </font>";
		return text;
	}

	public static Label getWarningLabel(String text){
		return new Label(warningText(text), Label.CONTENT_XHTML);
	}

	public static String errorText(String text) {
		text = "<font color = \"red\"><b>" + "ERROR: " + text + "</b> </font>";
		return text;
	}

	public static String hilight(String text) {
		return "<b>" + text + "</b>";
	}

	public static String notificationText(String text) {
		text = "<font color = \"green\"><b>" + text + "</b> </font>";
		return text;
	}

	public static String localWarningText(String text){
		return warningText("LOCAL WARNING - " + text);
	}

	public static String localErrorText(String text) {
		return errorText("LOCAL ERROR - " + text);

	}

	public static boolean isErrorOrWarningMsg(String msg) {
		if (msg.startsWith("<font color = \"red\">")) return true;
		else return false;
	}

	public static String actionMessage(String nick, String action, String message, boolean isActionType) {
		if (message == null || message.length() < 1)
			return "<font color = \"green\"><b>" + (!isActionType?"-&gt ":"* ") + nick + " " + action + "</b> </font>";
		else {
			message = "[" + message + "]";
			return "<font color = \"green\"><b>" + (!isActionType?"-&gt ":"* ") + nick + " " + action + "</b><br>" + message + " </font>";
		}
	}

	/**
	 *  We do not explicitely check if colors are turned on/off, because that is taken care of
	 *  in the irclibs already....
	 */
	public static String escapeXMLAndColorize(String toParse) {
		StringBuffer sb = new StringBuffer();

		for (int i=0; i<toParse.length(); i++){
			char c = toParse.charAt(i);
			if (c == 60){
				sb.append("&lt;");
				//sb.append((char) 11);
			} else if (c == 62){
				sb.append("&gt;");
				//sb.append((char)11);
			} else {
				sb.append(c);
			}
		}
		return (parseColorcodesAsHtml(sb)).toString();
	}


	public static StringBuffer parseColorcodesAsHtml(StringBuffer sb){

		try {
			int currentBG = -1;
			int currentFG = -1;
			int insideWhatSpan = -1; // -1 = is not inside a color span, 1 = foreground, 2 = background
			boolean isBold = false;
			boolean isUnderlined = false;
			StringBuffer result = new StringBuffer();

			for (int i=0; i<sb.length(); i++){
				char c = sb.charAt(i);
				//System.out.println((int)c);
				if (c == IRCConstants.COLOR_INDICATOR){
					String foreg = "";
					boolean hasBg = false;
					String backg = "";
					for (int j=i + 1;j<sb.length(); j++){
						if (isPositiveNumber(sb.charAt(j))) foreg+=Character.toString(sb.charAt(j));
						else break;
						if (foreg.length() >= 2) break;
					}
					//for (int j=i+foreg.length() + 1; j<sb.length(); j++){
					if (i+foreg.length() + 1<sb.length() && Character.toString(sb.charAt(i+foreg.length() + 1)).equals(",")) hasBg = true;
					//}
					if (hasBg){
						for (int j=i+foreg.length() + 2; j<sb.length(); j++){
							if (isPositiveNumber(sb.charAt(j))) backg+=Character.toString(sb.charAt(j));
							else break;
							if (backg.length() >= 2) break;
						}

					}
					int foreGroundColor = parseColorNumber(foreg, true);
					int backGroundColor = parseColorNumber(backg, false);

					// if both colors are parsed as -1, end-of-color indicator was probably intended -> remove all colors
					if (foreGroundColor == -1 && backGroundColor == -1){
						if (currentBG > -1) result.append("</span>");
						if (currentFG > -1) result.append("</span>");
						currentBG = -1;
						currentFG = -1;
						insideWhatSpan = -1;
					}
					if (foreGroundColor > -1){
						if (insideWhatSpan == -1){ // if no color yet (not bg/fg)
							result.append("<span class=\"fgcol" + foreGroundColor + "\">");
							insideWhatSpan = 1;
						} else if (insideWhatSpan == 1 && currentFG != foreGroundColor){ // if currently inside same span
							result.append("</span>");
							result.append("<span class=\"fgcol" + foreGroundColor + "\">");
						} else if (insideWhatSpan == 2){ // if currently inside background span
							if (currentFG > -1) { // if also currentlu inside a foreground span, close it too
								result.append("</span>");
							}
							result.append("<span class=\"fgcol" + foreGroundColor + "\">");
						}
						currentFG = foreGroundColor;
					}

					if (backGroundColor > -1){
						if (insideWhatSpan == -1){ // if no color yet (not bg/fg)
							result.append("<span class=\"bgcol" + backGroundColor + "\">");
							insideWhatSpan = 1;
						} else if (insideWhatSpan == 2 && currentBG != backGroundColor){ // if currently inside same span
							result.append("</span>");
							result.append("<span class=\"bgcol" + backGroundColor + "\">");
						} else if (insideWhatSpan == 1){ // if currently inside foreground span
							if (currentBG > -1) { // if also currentlu inside a foreground span, close it too
								result.append("</span>");
							}
							result.append("<span class=\"bgcol" + backGroundColor + "\">");
						}
						currentBG = backGroundColor;
					}


					// move i forward
					i = i + foreg.length();
					if (hasBg) i = i + backg.length() + 1; // +1 for ,
				}


				/* BOLD */

				else if (c == IRCConstants.BOLD_INDICATOR) { // 2 is actually bold, not as the irc constant
					if (isBold) result.append("</b>");
					else result.append("<b>");
					isBold = !isBold;
				}

				/* UNDERLINE */

				else if (c == IRCConstants.UNDERLINE_INDICATOR) { // 2 is actually bold, not as the irc constant
					if (isUnderlined) result.append("</u>");
					else result.append("<u>");
					isUnderlined = !isUnderlined;
				}

				/* COLOR END */
				else if (c == IRCConstants.COLOR_END_INDICATOR){
					if (currentBG > -1 && currentFG > -1){
						result.append("</span>");
						if (insideWhatSpan == 1) currentBG = -1;
						else if (insideWhatSpan == 2) currentFG = -1;
					} else if (currentBG > -1 || currentFG > -1){
						result.append("</span>");
						insideWhatSpan = -1;
						currentBG = -1;
						currentFG = -1;
					}

					if (isBold) {
						result.append("</b>");
						isBold = false;
					}

					if (isUnderlined) {
						result.append("</u>");
						isUnderlined = false;
					}

				} else {
					result.append(c);
				}
			}

			if (currentBG >-1) result.append("</span>");
			if (currentFG >-1) result.append("</span>");
			if (isBold) result.append("</b>");
			if (isUnderlined) result.append("</u>");

			return result;
		} catch (Exception e){
			e.printStackTrace();
			return sb; // return no colors if exception thrown
		}
	}


//	<html>
//	The due date is <FONT
//	style="BACKGROUND-COLOR: #FF0000" color = "red">jag heter johan
//	och jag byter
//	<font color = "#9933FF">
//	om nu som dе
//	</font>
//	</font>
//
//	slutet gott


	/**
	 * Returns the name of the color
	 */
	private static int parseColorNumber(String colnum, boolean isFg) {
		if (colnum.length() == 0 || colnum == null) return -1;
		if (colnum.length() > 2) {
			log.warn("Trying to parse a color number: " + colnum + " that has greater size than two characters! SKIPPING!");
			return -1;
		}
		int icolnum;
		try {
			icolnum = Integer.parseInt(colnum);
		} catch (NumberFormatException e){
			log.warn(colnum + " is not a number");
			return -1;
		}

		return (icolnum % 16);

	}

	public static boolean isPositiveNumber(char c){
		int i = -1;
		try {
			i = Integer.parseInt(Character.toString(c));
		} catch (NumberFormatException nfe){
			return false;
		}
		return i >= 0;
	}

	public static String getLastWordFromSentence(String sentence) {
		try {
			String splitted[] = (sentence.trim()).split("\\s");
			return splitted[splitted.length - 1];
		} catch (Exception e){
			e.printStackTrace();
			return "";
		}
	}

	public static String replaceLastWordInSentence(String sentence, String word){
		try {
			int i;
			for (i = sentence.length(); i > 0; i--){
				if (sentence.substring(i - 1).contains(" ")) break;
			}

			return (sentence.substring(0, i) + word);
		} catch (Exception e){
			e.printStackTrace();
			return "";
		}

	}

	public static String[] commands = {"away","deop","devoice","invite",
		"ison","join","kick","list","mode",
		"names","nick","notice","op","part","pong",
		"privmsg","quit","topic","userhost",
		"voice", "who","whois","whowas", "me"};

	public static String getUniqueCommand(String action) {
		ArrayList<String> lst = new ArrayList<String>();
		int index = -1;
		for (int i=0; i<commands.length; i++){
			if (commands[i].toLowerCase().startsWith(action.toLowerCase())) lst.add(commands[i]);
		}

		if (lst.size() == 1) return lst.get(0);
		else if (lst.size() == 0) return null;
		else return "";
	}

	/**
	 * Parses a text and if http:// is found, it will be outputted as an html-link
	 */
	public static String parseHttpAsLink(String text){
		if (!text.contains("http://")) return text;

		StringBuffer result = new StringBuffer();
		for (int i=0; i<text.length(); i++){
			char c = text.charAt(i);
			if (text.regionMatches(i, "http://", 0, 7)){
				StringBuffer result2 = new StringBuffer();
				i += 7;
				for (; i<text.length(); i++){
					if (text.substring(i, i + 1).matches("\\s"))
						break;

					result2.append(text.charAt(i));
				}
				result.append("<a href=\"http://" + result2 +"\">");
				result.append("http://" + result2 + "</a> ");
			} else result.append(c);
		}

		return result.toString();
	}

}
