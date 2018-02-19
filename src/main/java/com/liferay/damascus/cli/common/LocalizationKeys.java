package com.liferay.damascus.cli.common;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class LocalizationKeys {

	private static final Pattern[] _patterns = {
			Pattern.compile("FriendlyURLNormalizerUtil\\.normalize\\(\"(.*)\"\\)"),
			Pattern.compile("<liferay-ui:success\\s+key=\"[\\S]*\"\\s+message=\"([\\S]*)\" \\/>"),
			Pattern.compile("<liferay-ui:error\\s+key=\"[\\S]*\"\\s+message=\"([\\S]*)\" \\/>"),
			Pattern.compile("LanguageUtil.get\\(\\s*request\\s*,\\s*\"(\\S*)\"\\)"),
			Pattern.compile("LanguageUtil.get\\(\\s*request\\s*,\\s*\\\\\"(\\S*)\\\\\"\\)"),
			Pattern.compile("<liferay-ui:message\\s*key=\"(\\S*)\"\\s*\\/>"),
			Pattern.compile("<aui:button[^>]?\\svalue=\"(\\S*)\"\\s*\\/>")
	};
	
	private static final String[] _extensions = new String[] { "jspf", "jsp" };
	private static final String[] _exclude = new String[] {"title", "asset-title", "or", "comments", "are-you-sure-you-want-to-delete-the-selected-entries" };
	private static Set<String> i18nKeys;
	
	
	public static Set<String> getI18nKeys(String path) throws IOException{
		File directory = new File(path);
		i18nKeys = new HashSet<String>();

		if(directory.isDirectory()){
		
			// Get Files
			List<File> webFiles = (List<File>) FileUtils.listFiles(directory, _extensions, true);
			
			for(File webFile: webFiles){
				extractKeysFromFile(webFile);
			}
			
		}
		
		return i18nKeys;
	}
	
	
	private static void extractKeysFromFile(File file) throws IOException{

        String webFileContent = FileUtils.readFileToString(file, DamascusProps.FILE_ENCODING);
        for(Pattern pattern: _patterns){
        	extractKeysFromString(webFileContent, pattern);
        }
		
	}
	
	private static void extractKeysFromString(String value, Pattern pattern){
		Matcher matcher = pattern.matcher(value);

		while (matcher.find()) {
		    String matchString = matcher.group(1);
		    if(!matchString.startsWith("<%=") && !Arrays.asList(_exclude).contains(matchString)){
		    	i18nKeys.add(CaseUtil.camelCaseToDashCase(matchString));
		    }
		}
	}
	
}
