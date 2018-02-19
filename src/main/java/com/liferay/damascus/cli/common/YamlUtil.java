package com.liferay.damascus.cli.common;


import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

public class YamlUtil {

	public static String convertToJson(String yamlString) {
	    Yaml yaml= new Yaml();
	    Map<String,Object> map= (Map<String, Object>) yaml.load(yamlString);

	    JSONObject jsonObject=new JSONObject(map);
	    return jsonObject.toString();
	}
	
	
	public static String convertToJson(File baseYamlFile) throws IOException{
		String yamlString = FileUtils.readFileToString(baseYamlFile, "utf-8");
		
		return convertToJson(yamlString);
	}
}
