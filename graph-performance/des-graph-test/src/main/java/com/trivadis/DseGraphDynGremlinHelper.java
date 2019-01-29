package com.trivadis;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphNode;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.datastax.driver.dse.graph.SimpleGraphStatement;

public class DseGraphDynGremlinHelper {

	private GraphMetrics graphMetrics = null;
	
	private Map<String,Map<String,Object>> bindVars = new HashMap();
	private StringBuffer stmt = new StringBuffer();
	
	private DseSession session;
	


	private String fmtLoadVertex(String vertexName, String vertexLabel, String propertyKey, Object propertyKeyValue) {
		StringBuffer stmt = new StringBuffer();
		StrSubstitutor subst = null;
		
		Map<String,Object> params = new HashMap();
		params.put("vertexName", vertexName);
		params.put("vertexLabel", vertexLabel);
		params.put("propertyKey", propertyKey);
		params.put("propertyKeyValue", propertyKeyValue);
		subst = new StrSubstitutor(params);

		stmt.append(subst.replace("${vertexName} = g.V().has('${vertexLabel}', '${propertyKey}', ${propertyKeyValue}).next()"));
		
		return stmt.toString();
	}
	
	private String fmtCreateOrUpdateVertex(String vertexName, String vertexLabel, String propertyKey, String propertyKeyValue, String... propertiesParam) {
		StringBuffer stmt = new StringBuffer();
		StrSubstitutor subst = null;
		
		Map<String,Object> params = new HashMap();
		params.put("vertexName", vertexName);
		params.put("vertexLabel", vertexLabel);
		params.put("propertyKey", propertyKey);
		params.put("propertyKeyValue", propertyKeyValue);
		subst = new StrSubstitutor(params);
		stmt.append(subst.replace("if (!nof.containsKey('vertex.lookup.${vertexLabel}')) nof['vertex.lookup.${vertexLabel}'] = 0")).append("\n");
		stmt.append(subst.replace("nof['vertex.lookup.${vertexLabel}']++")).append("\n");
		stmt.append(subst.replace("if (!nof.containsKey('vertex.create.${vertexLabel}')) nof['vertex.create.${vertexLabel}'] = 0")).append("\n");
		stmt.append(subst.replace("${vertexName} = g.V().has('${vertexLabel}', '${propertyKey}', ${propertyKeyValue}).tryNext().orElseGet { nof['vertex.create.${vertexLabel}']++; g.addV('${vertexLabel}').property('${propertyKey}', ${propertyKeyValue}).next() }"));
		params.clear();
		for (int i=0; i<propertiesParam.length/2 ; i++) {
			params.put("vertexName", vertexName);
			params.put("propertyKey", propertiesParam[i*2]);
			params.put("propertyValue", propertiesParam[i*2+1]);

			subst = new StrSubstitutor(params);
			stmt.append("\n");
			stmt.append(subst.replace("${vertexName}.property('${propertyKey}',${propertyValue})"));
		}
		
		return stmt.toString();
	}
	
	private String fmtCreateOrUpdateEdge(String edgeName, String fromVertexName, String toVertexName, String edgeLabel, String... propertiesParam) {
		StringBuffer stmt = new StringBuffer();
		StrSubstitutor subst = null;
		
		Map<String,String> params = new HashMap();
		params.put("edgeName", edgeName);
		params.put("fromVertexName", fromVertexName);
		params.put("toVertexName", toVertexName);
		params.put("edgeLabel", edgeLabel);
		subst = new StrSubstitutor(params);
		
		stmt.append(subst.replace("if (!nof.containsKey('edge.lookup.${edgeLabel}')) nof['edge.lookup.${edgeLabel}'] = 0")).append("\n");
		stmt.append(subst.replace("nof['edge.lookup.${edgeLabel}']++")).append("\n");
		stmt.append(subst.replace("if (!nof.containsKey('edge.create.${edgeLabel}')) nof['edge.create.${edgeLabel}'] = 0")).append("\n");
		stmt.append(subst.replace("if (!g.V(${fromVertexName}).out('${edgeLabel}').hasId(${toVertexName}.id()).hasNext()) {\n"));
		stmt.append(subst.replace("\t\t${edgeName} = g.V(${fromVertexName}).as('f').V(${toVertexName}).as('t').addE('${edgeLabel}').from('f').next()\n"));
		stmt.append(subst.replace("\t\tnof['edge.create.${edgeLabel}']++;\n"));
		params.clear();
		for (int i=0; i<propertiesParam.length/2 ; i++) {
			params.put("edgeName", edgeName);
			params.put("propertyKey", propertiesParam[i*2]);
			params.put("propertyValue", propertiesParam[i*2+1]);

			subst = new StrSubstitutor(params);
			stmt.append("\n");
			stmt.append(subst.replace("${edgeName}.property('${propertyKey}','${propertyValue}')"));
		}
		stmt.append("\t}");
		return stmt.toString();
	}

	public DseGraphDynGremlinHelper(DseSession session) {
		this.session = session;
		stmt.append("nof = [:]").append("\n");
		
		stmt.append("int i = 0").append("\n");
	}
	
	public void addLoadVertex(String vertexName, String vertexLabel, String propertyKey, Object propertyKeyValue) {
		final String bindVar = "b" + StringUtils.capitalize(vertexName);
		Map<String,Object> bindVarsMap = new HashMap<String,Object>();

		bindVarsMap.put(propertyKey, propertyKeyValue);
		bindVars.put(bindVar, bindVarsMap);

		stmt.append(fmtLoadVertex(vertexName, vertexLabel, propertyKey, bindVar+"."+propertyKey));
		stmt.append("\n");
	}

	public void addCreateVertex(String vertexName, String vertexLabel, String propertyKey, String propertyKeyValue, Object... propertyParams) {
		final String bindVar = "b" + StringUtils.capitalize(vertexName);
		List<String> params = new ArrayList<String>();
		Map<String,Object> bindVarsMap = new HashMap<String,Object>();
		
		if (propertyParams != null) {
			for (int i=0; i<propertyParams.length/2 ; i++) {
				if (propertyParams[i*2+1] != null) {
					params.add((String)propertyParams[i*2]);
					params.add(bindVar + "." + (String)propertyParams[i*2]);
					
					bindVarsMap.put((String)propertyParams[i*2], propertyParams[i*2+1]);
				}
			}
		}
		bindVarsMap.put(propertyKey, propertyKeyValue);
		bindVars.put(bindVar, bindVarsMap);
		
		//stmt.append("try {").append("\n");		
		stmt.append(fmtCreateOrUpdateVertex(vertexName, vertexLabel, propertyKey, bindVar+"."+propertyKey, params.toArray(new String[0])));
		//stmt.append("} catch(Exception e) { println(\"").append(vertexName).append("\")\n throw e}").append("\n");
		stmt.append("\n");
	}
	
	
	public void addCreateEdge(String edgeName, String fromVertexName, String toVertexName, String edgeLabel, Object... propertyParams) {
		final String bindVar = "b" + StringUtils.capitalize(edgeName);

		List<String> params = new ArrayList<String>();
		Map<String,Object> bindVarsMap = new HashMap<String,Object>();
		
		if (propertyParams != null) {
			
			for (int i=0; i<propertyParams.length/2 ; i++) {
				if (propertyParams[i*2+1] != null) {
					params.add((String)propertyParams[i*2]);
					params.add(bindVar + "." + (String)propertyParams[i*2]);
				
					bindVarsMap.put((String)propertyParams[i*2], propertyParams[i*2+1]);
				}
			}
			if (!bindVarsMap.isEmpty())
				bindVars.put(bindVar, bindVarsMap);
		}
		
		stmt.append(fmtCreateOrUpdateEdge(edgeName, fromVertexName, toVertexName, edgeLabel, params.toArray(new String[0])));
		stmt.append("\n");
		
	}
	
	public void addCreateVertices(String vertexName, String vertexLabel, String propertyKey, List<Object> propertyKeyValues, Object ... propertyParams) {
		List<Map<String,Object>> params = new ArrayList<Map<String,Object>>();

		if (propertyKeyValues != null && propertyKeyValues.size() > 0) {
		
			for (Object o : propertyKeyValues) {
				Map<String,Object> map = new HashMap<String,Object>();
	
				if (propertyParams != null) {
					for (int i=0; i<propertyParams.length/2 ; i++) {
						if (propertyParams[i*2+1] != null) {
							map.put((String)propertyParams[i*2], propertyParams[i*2+1]);
						}
					}
				}
				params.add(map);
			}
			addCreateVertices(vertexName, vertexLabel, propertyKey, propertyKeyValues, params);
		}
	}

	public void addCreateVertices(String vertexName, String vertexLabel, String propertyKey, List<Object> propertyKeyValues, List<Map<String,Object>> propertyParams) {
		final String bindVar = "b" + StringUtils.capitalize(vertexName);
		Map<String,Object> bindVarsMap = new HashMap<String,Object>();

		if (propertyKeyValues != null && propertyKeyValues.size() > 0) {
			stmt.append("i = 0\n");
			stmt.append("Vertex[] ").append(vertexName).append(" = new Vertex[").append(propertyKeyValues.size()).append("]\n");
			stmt.append("for (Object keyValue : " + bindVar + ".propertyKeyValues) {");
			stmt.append("\n");
	
			stmt.append("\t").append(fmtCreateOrUpdateVertex(vertexName + "[i]", vertexLabel, propertyKey, "keyValue")).append("\n");
			stmt.append("\t").append("Map<String,Object> params = " + bindVar + ".params[i]").append("\n");
			stmt.append("\t").append("if (params != null)").append("\n");
			stmt.append("\t").append("for (String key : params.keySet()) {").append("\n");
			stmt.append("\t\t").append(vertexName + "[i].property(key, params.get(key))").append("\n");
			stmt.append("\t").append("}").append("\n");
			stmt.append("i++").append("\n");
			stmt.append("}").append("\n");
			
			bindVarsMap.put("propertyKeyValues", propertyKeyValues);
			bindVarsMap.put("params", propertyParams);
			bindVars.put(bindVar, bindVarsMap);
		}
	}
	
	public void addCreateEdges(String edgeName, String fromVertexName, int fromVertexSize, String toVertexName, int toVertexSize, String edgeLabel, Object ... propertyParams) {
		List<Map<String,Object>> params = new ArrayList<Map<String,Object>>();

		if (toVertexSize > 0) {
		
			for (int i = 0; i < toVertexSize; i++) {
				Map<String,Object> map = new HashMap<String,Object>();
	
				if (propertyParams != null) {
					for (int j=0; j<propertyParams.length/2 ; j++) {
						if (propertyParams[j*2+1] != null) {
							map.put((String)propertyParams[j*2], propertyParams[j*2+1]);
						}
					}
				}
				params.add(map);
			}
			addCreateEdges(edgeName, fromVertexName, fromVertexSize, toVertexName, toVertexSize, edgeLabel, params);
		}	
	}

	public void addCreateEdges(String edgeName, String fromVertexName, int fromVertexSize, String toVertexName, int toVertexSize, String edgeLabel, List<Map<String,Object>> propertyParams) {
		final String bindVar = "b" + StringUtils.capitalize(edgeName);
		Map<String,Object> bindVarsMap = new HashMap<String,Object>();
		
		if (toVertexSize > 0) {
			stmt.append("Edge[] ").append(edgeName).append(" = new Edge[").append(toVertexSize).append("]\n");
			stmt.append("for (i = 0; i < " + bindVar + ".count; i++) {");
			stmt.append("\n");
	
			stmt.append("\t").append(fmtCreateOrUpdateEdge(edgeName + "[i]", fromVertexName, toVertexName + "[i]", edgeLabel)).append("\n");
			stmt.append("\t").append("Map<String,Object> params = " + bindVar + ".params[i]").append("\n");
			stmt.append("\t").append("if (params != null)").append("\n");
			stmt.append("\t").append("for (String key : params.keySet()) {").append("\n");
			stmt.append("\t\t").append(edgeName + "[i]" + ".property(key, params.get(key))").append("\n");
			stmt.append("\t").append("}").append("\n");
			stmt.append("}").append("\n");
	
			//bindVarsMap.put("propertyKeyValues", propertyKeyValues);
			bindVarsMap.put("count", toVertexSize);
			bindVarsMap.put("params", propertyParams);
			bindVars.put(bindVar, bindVarsMap);
		}
	}	

	public void execute(String name, boolean logIt) {
		//stmt.append("return nofV + ',' + nofE").append("\n");

		stmt.append("return nof").append("\n");
		SimpleGraphStatement sgs = new SimpleGraphStatement(stmt.toString());
		for (String key : bindVars.keySet()) {
			sgs.set(key, bindVars.get(key));
		}
		try {
			if (logIt) {
				System.out.println(stmt.toString());
			}

			graphMetrics = new GraphMetrics();
			
			Long t = System.currentTimeMillis();
			GraphResultSet rs = session.executeGraph(sgs);
			Long tAfter = System.currentTimeMillis() - t;
			
			for (GraphNode gn : rs.all()) {
				Map<String,Integer> metrics = new HashMap<String,Integer>();
				Map<String,Object> m = gn.asMap();
				for (String key : m.keySet()) {
					metrics.put(key, (Integer)m.get(key));
				}
				graphMetrics.add(metrics);
			}

		} catch (RuntimeException re) {
			System.out.println(stmt.toString());
			System.out.println(fmtBindVars());
			re.printStackTrace();
		}
		
		//session.close();
	}		
	
	public void execute(String name) {
		execute(name, false);
	}
	
	public String fmtBindVars() {
		StringBuffer result = new StringBuffer();
		for (String key : bindVars.keySet()) {
			result.append(key).append("==>").append("(");
			String sep = "";
			for (String innerKey : bindVars.get(key).keySet()) {
				result.append(sep).append(innerKey).append("=").append(bindVars.get(key).get(innerKey)).append("[").append(bindVars.get(key).get(innerKey).getClass().getCanonicalName()).append("]");
				sep = ",";
			}
			result.append(")").append("\n");
		}
		return result.toString();
	}

	public GraphMetrics getGraphMetrics() {
		return graphMetrics;
	}

				
}
