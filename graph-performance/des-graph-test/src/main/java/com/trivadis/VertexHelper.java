package com.trivadis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.text.StrSubstitutor;

import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.Edge;
import com.datastax.driver.dse.graph.GraphNode;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.datastax.driver.dse.graph.SimpleGraphStatement;
import com.datastax.driver.dse.graph.Vertex;
import com.google.common.collect.ImmutableMap;

public class VertexHelper {
	
	/**
	 * Remove a property with a NULL value from the Object array
	 * @param properties
	 * @return
	 */
	public static Object[] removeNULLPropertyValue(Object ... properties) {
		List<Object> propertiesCopy = new ArrayList<Object>();
		int x = 0;
		for (int i=0; i<properties.length/2 ; i++) {
			String key = (String)properties[x];
			Object value = properties[++x];
			if (value != null) {
				propertiesCopy.add(key);
				propertiesCopy.add(value);
			}
			x++;
		}
		return propertiesCopy.toArray();
	}
	

	
	public static Vertex getVertexByLabelAndPropertyKey(DseSession session, String label, String propertyKey, Object propertyKeyValue) {
		Vertex vertex = null;

		SimpleGraphStatement s = new SimpleGraphStatement("g.V().has(vertexLabel, propertyKey ,propertyKeyValue)")
												.set("vertexLabel", label)
												.set("propertyKey", propertyKey)
												.set("propertyKeyValue", propertyKeyValue);
		GraphResultSet resultSet = session.executeGraph(s);
		GraphNode node = resultSet.one();
		
		if (node != null) {
			vertex = node.asVertex();
		}
		
		return vertex;
	}

	public static String fmtLoadVertex(String vertexName, String vertexLabel, String propertyKey, String propertyKeyValue) {
		StringBuffer stmt = new StringBuffer();
		StrSubstitutor subst = null;
		
		Map<String,String> params = new HashMap();
		params.put("vertexName", vertexName);
		params.put("vertexLabel", vertexLabel);
		params.put("propertyKey", propertyKey);
		params.put("propertyKeyValue", (String)propertyKeyValue);
		subst = new StrSubstitutor(params);
		stmt.append(subst.replace("${vertexName} = g.V().has('${vertexLabel}', '${propertyKey}', '${propertyKeyValue}').next()"));
		
		return stmt.toString();
	}
	
	public static String fmtCreateOrUpdateVertex(String vertexName, String vertexLabel, String propertyKey, String propertyKeyValue, String... propertiesParam) {
		StringBuffer stmt = new StringBuffer();
		StrSubstitutor subst = null;
		
		Map<String,String> params = new HashMap();
		params.put("vertexName", vertexName);
		params.put("vertexLabel", vertexLabel);
		params.put("propertyKey", propertyKey);
		params.put("propertyKeyValue", (String)propertyKeyValue);
		subst = new StrSubstitutor(params);
		stmt.append(subst.replace("${vertexName} = g.V().has('${vertexLabel}', '${propertyKey}', ${propertyKeyValue}).tryNext().orElseGet { nofNewV++; g.addV('${vertexLabel}').property('${propertyKey}', ${propertyKeyValue}).next() }"));
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
	
	public static String fmtCreateOrUpdateEdge(String edgeName, String fromVertexName, String toVertexName, String edgeLabel, String... propertiesParam) {
		StringBuffer stmt = new StringBuffer();
		StrSubstitutor subst = null;
		
		Map<String,String> params = new HashMap();
		params.put("edgeName", edgeName);
		params.put("fromVertexName", fromVertexName);
		params.put("toVertexName", toVertexName);
		params.put("edgeLabel", edgeLabel);
		subst = new StrSubstitutor(params);
		
		//if (g.V(tweet).out("uses").hasId(term[2].id()).hasNext()) {
		//stmt.append(subst.replace("${edgeName} = g.V(${fromVertexName}).out('${edgeLabel}').V(${toVertexName}).tryNext().orElseGet { ${fromVertexName}.addEdge('${edgeLabel}', ${toVertexName}) }"));	
		stmt.append(subst.replace("if (!g.V(${fromVertexName}).out('${edgeLabel}').hasId(${toVertexName}.id()).hasNext()) {\n"));
		stmt.append(subst.replace("\t\t${edgeName} = g.V(${fromVertexName}).as('f').V(${toVertexName}).as('t').addE('${edgeLabel}').from('f').next()\n"));
		stmt.append(subst.replace("\t\tnofE++\n"));
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
		//stmt.append(subst.replace("${edgeName} = g.V(${fromVertexName}).out('${edgeLabel}').hasId(${toVertexName}.id()).tryNext().orElseGet { g.V(${fromVertexName}.id()).as('fromV').V(${toVertexName}.id()).addE('${edgeLabel}').from('fromV') }"));
		//stmt.append(subst.replace(" ${edgeName} = g.V(${fromVertexName}).as('fromV').V(${toVertexName}).addE('${edgeLabel}').from('fromV') "));
		
		

		return stmt.toString();
	}

		
	public static Vertex getVertexAndUpdateProperties(DseSession session, String vertexLabel, String propertyKey, Object propertyKeyValue,  Object... propertiesParam) {
		Vertex vertex = null;
		// remove properties with NULL values
		Object[] properties = removeNULLPropertyValue(propertiesParam);

		vertex = VertexHelper.getVertexByLabelAndPropertyKey(session, vertexLabel, propertyKey, propertyKeyValue);
/*		
		int x = 0;
		for (int i=0; i<properties.length/2 ; i++) {
			String key = (String)properties[x];
			Object o = properties[++x];
			if (vertex.properties(key) != null) {
				vertex.property(key, o);
			} else {
				vertex.property(key, o);
			}
			x++;
		}
*/		
		return vertex;
	}
	
	public static Pair<String, Map<String, Object>> formatPropertyStatement(Object[] properties) {
		Map<String,Object> param = new HashMap<String,Object>();
		StringBuffer stmt = new StringBuffer();
		for (int i = 0; i < properties.length/2; i++) {
			int k = i * 2;
			int v = i * 2 + 1;
			String paramName = "propertyParam" + i;
			stmt.append("v.property(\"").append(properties[k]).append("\",b.").append(paramName).append(")" + "\n");
			param.put(paramName, properties[v]);
			
		}
		return new Pair<String, Map<String,Object>>(stmt.toString(), param);
	}

	public static Vertex createVertex(DseSession session, String vertexLabel, String propertyKey, Object propertyKeyValue,  Object... propertiesParam) {
		Vertex vertex = null;
		// remove properties with NULL values
		Object[] properties = VertexHelper.removeNULLPropertyValue(propertiesParam);
		Pair<String, Map<String,Object>> propertyParams = formatPropertyStatement(properties);
		String stmt = ""
						+ "Vertex v" + "\n"
						+ "v = graph.addVertex(label, b.vertexLabel, b.propertyKey, b.propertyKeyValue)" + "\n"
						+ propertyParams.getLeft() + "\n"
						+ "return v";
//		System.out.println(stmt);

		Map<String,Object> map = ImmutableMap.<String,Object>of("vertexLabel", vertexLabel, "propertyKey", propertyKey, "propertyKeyValue", propertyKeyValue);

		vertex = session.executeGraph(new SimpleGraphStatement(stmt)
								.set("b", ImmutableMap.<String,Object>builder().putAll(map).putAll(propertyParams.getRight()).build()
								)).one().asVertex();
		return vertex;
	}

	public static Vertex createOrUpdateVertex(DseSession session, boolean updateOnly, String vertexLabel, String propertyKey, Object propertyKeyValue,  Object... propertiesParam) {
		Vertex vertex = null;
		// remove properties with NULL values
		Object[] properties = VertexHelper.removeNULLPropertyValue(propertiesParam);
		if (!updateOnly) {
			    Pair<String, Map<String,Object>> propertyParams = formatPropertyStatement(properties);
				String stmt = ""
						+ "Vertex v" + "\n"
						+ "GraphTraversal gt = g.V().has(b.vertexLabel, b.propertyKey, b.propertyKeyValue)" + "\n"
						+ "if (!gt.hasNext()) {"  + "\n"
						+ "		v = graph.addVertex(label, b.vertexLabel, b.propertyKey, b.propertyKeyValue)" + "\n"
						+ "} else {" + "\n"
						+ "		v = gt.next()" + "\n"
						+ "}" + "\n"
						+ propertyParams.getLeft() + "\n"
						+ "return v";
				System.out.println(stmt);

				Map<String,Object> map = ImmutableMap.<String,Object>of("vertexLabel", vertexLabel, "propertyKey", propertyKey, "propertyKeyValue", propertyKeyValue);

				vertex = session.executeGraph(new SimpleGraphStatement(stmt)
								.set("b", ImmutableMap.<String,Object>builder().putAll(map).putAll(propertyParams.getRight()).build()
								)).one().asVertex();
		} else {
			vertex = VertexHelper.getVertexAndUpdateProperties(session, vertexLabel, propertyKey, propertyKeyValue, properties);
		}
		return vertex;
	}

	public static void createOrUpdateVertices(DseSession session, String vertexLabel, String propertyKey, List<Object> propertyKeyValues,  Object... propertiesParam) {
		Vertex vertex = null;
		// remove properties with NULL values
		Object[] properties = VertexHelper.removeNULLPropertyValue(propertiesParam);
		Pair<String, Map<String,Object>> propertyParams = formatPropertyStatement(properties);
		String stmt = ""
						+ "List<Vertex> l = new ArrayList<Vertex>()"
						+ "for (Object propertyKeyValue : b.propertyKeyValues) {"
						+ "  Vertex v" + "\n"
						+ "  GraphTraversal gt = g.V().has(b.vertexLabel, b.propertyKey, propertyKeyValue)" + "\n"
						+ "  if (!gt.hasNext()) {"  + "\n"
						+ "	  	v = graph.addVertex(label, b.vertexLabel, b.propertyKey, propertyKeyValue)" + "\n"
						+ "  } else {" + "\n"
						+ "  	v = gt.next()" + "\n"
						+ "  }" + "\n"
						+ propertyParams.getLeft() + "\n"
						+ "  l.add(v)" + "\n"
						+ " }" + "\n"
						+ "return l";
//		System.out.println(stmt);

		Map<String,Object> map = ImmutableMap.<String,Object>of("vertexLabel", vertexLabel, "propertyKey", propertyKey, "propertyKeyValues", propertyKeyValues);

		session.executeGraph(new SimpleGraphStatement(stmt)
								.set("b", ImmutableMap.<String,Object>builder().putAll(map).putAll(propertyParams.getRight()).build()
								));

	}

	public static Vertex createOrUpdateVertexAndEdge(DseSession session, String vertexLabel, String propertyKey, 
										Object propertyKeyValue, String edgeLabel, Vertex toVertex, Object... propertiesParam) {
		Vertex vertex = null;
		// remove properties with NULL values
		Object[] properties = VertexHelper.removeNULLPropertyValue(propertiesParam);
		
		Pair<String, Map<String,Object>> propertyParams = formatPropertyStatement(properties);
		String stmt = ""
						+ "Vertex v" + "\n"
						+ "GraphTraversal gt = g.V().has(b.vertexLabel, b.propertyKey, b.propertyKeyValue)" + "\n"
						+ "if (!gt.hasNext()) {"  + "\n"
						+ "		v = graph.addVertex(label, b.vertexLabel, b.propertyKey, b.propertyKeyValue)" + "\n"
						+ "} else {" + "\n"
						+ "		v = gt.next()" + "\n"
						+ "}" + "\n"
						+ "Vertex to = g.V(t).next(); v.addEdge(b.edgeLabel, to)" + "\n"
						+ propertyParams.getLeft() + "\n"
						+ "return v";
//		System.out.println(stmt);

		Map<String,Object> map = ImmutableMap.<String,Object>of("vertexLabel", vertexLabel, "propertyKey", propertyKey, "propertyKeyValue", propertyKeyValue, "edgeLabel", edgeLabel);

		vertex = session.executeGraph(new SimpleGraphStatement(stmt)
								.set("t", toVertex)
								.set("b", ImmutableMap.<String,Object>builder().putAll(map).putAll(propertyParams.getRight()).build()
								)).one().asVertex();
		return vertex;
	}

	public static Vertex createOrUpdateVertexAndEdges(DseSession session, String fromVertexLabel, String fromPropertyKey,
								Object fromPropertyKeyValue, String edgeLabel, String toVertexLabel, String toPropertyKey, 
								List<Object> toPropertyKeyValues, Object... propertiesParam) {
		Vertex vertex = null;
		// remove properties with NULL values
		Object[] properties = VertexHelper.removeNULLPropertyValue(propertiesParam);

		Pair<String, Map<String,Object>> propertyParams = formatPropertyStatement(properties);
		String stmt = ""
						+ "Vertex from" + "\n"
						+ "GraphTraversal gt" + "\n"
						+ "gt = g.V().has(b.fromVertexLabel, b.fromPropertyKey, b.fromPropertyKeyValue)" + "\n"
						+ "if (!gt.hasNext()) {"  + "\n"
						+ "		from = graph.addVertex(label, b.fromVertexLabel, b.fromPropertyKey, b.fromPropertyKeyValue)" + "\n"
						+ "} else {" + "\n"
						+ "		from = gt.next()" + "\n"
						+ "}" + "\n"
				
						+ "for (Object toPropertyKeyValue : b.toPropertyKeyValues) {" + "\n"
						+ "  gt = g.V().has(b.toVertexLabel, b.toPropertyKey, toPropertyKeyValue)" + "\n"
						+ "  if (!gt.hasNext()) {"  + "\n"
						+ "	  	to = graph.addVertex(label, b.toVertexLabel, b.toPropertyKey, toPropertyKeyValue)" + "\n"
						+ "  } else {" + "\n"
						+ "  	to = gt.next()" + "\n"
						+ "  }" + "\n"
//						+ propertyParams.getLeft() + "\n"
						+ "	from.addEdge(b.edgeLabel, to)" + "\n"
						+ " }" + "\n"
 						+ "return from";
//		System.out.println(stmt);

		vertex = session.executeGraph(new SimpleGraphStatement(stmt)
								.set("b", ImmutableMap.<String,Object>builder()
												.put("fromVertexLabel", fromVertexLabel)
												.put("fromPropertyKey", fromPropertyKey)
												.put("fromPropertyKeyValue", fromPropertyKeyValue)
												.put("edgeLabel", edgeLabel)
												.put("toVertexLabel", toVertexLabel)
												.put("toPropertyKey", toPropertyKey)
												.put("toPropertyKeyValues", toPropertyKeyValues)
												.build()
								)).one().asVertex();
		return vertex;
	}

	public static Edge createOrUpdateEdge(DseSession session, boolean ifNotExists, String edgeLabel, Vertex fromVertex, Vertex toVertex, Object... propertiesParam) {
		Edge edge = null;
		String stmt = null;

		// remove properties with NULL values
		Object[] properties = VertexHelper.removeNULLPropertyValue(propertiesParam);

		if (ifNotExists) {
			stmt = "Vertex from = g.V(f).next(); Vertex to = g.V(t).next(); if (!g.V(f).out(b.edgeLabel).V(t).hasNext()) { from.addEdge(b.edgeLabel, to) }";
		} else {  
			stmt = "Vertex from = g.V(f).next(); Vertex to = g.V(t).next(); from.addEdge(b.edgeLabel, to)";
		}
//		System.out.println(stmt);
		
		edge = session.executeGraph(new SimpleGraphStatement(stmt)
						.set("f", fromVertex)
						.set("t", toVertex)
						.set("b", ImmutableMap.<String,Object>of("edgeLabel", edgeLabel, "propertiesParam", properties))).one().asEdge();
		return edge;
	}
	
	public static boolean hasEdge(DseSession session, String edgeLabel, Vertex fromVertex, Vertex toVertex) {
		boolean result = false;
		String stmt = "g.V(f).out(b.edgeLabel).V(t).hasNext()";
		result = session.executeGraph(new SimpleGraphStatement(stmt)
						.set("f", fromVertex)
						.set("t", toVertex)
						.set("b", ImmutableMap.<String,Object>of("edgeLabel", edgeLabel))).one().asBoolean();
		return result;
		
	}
}
