package refactminer;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.eclipse.jgit.lib.Repository;
import org.json.simple.JSONObject;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;

import gr.uom.java.xmi.decomposition.CompositeStatementObject;
import gr.uom.java.xmi.decomposition.StatementObject;
import gr.uom.java.xmi.decomposition.UMLOperationBodyMapper;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;

public class Main {
	public static void main(String[] args) {
		GitService gitService = new GitServiceImpl();
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

		try {
			JSONObject jsonObject;
			JSONParser parser = new JSONParser();
			jsonObject = (JSONObject) parser.parse(new FileReader(
					"/Users/marcosgenesis/eclipse-workspace/refactminer/src/main/java/refactminer/repos.json"));

			System.out.println(jsonObject.values());
			for (Object item : jsonObject.values()) {

				final JSONObject objeto;
				objeto = (JSONObject) parser.parse(String.valueOf(item));
				System.out.println(objeto.get("sha1"));

				Repository repo = gitService.cloneIfNotExists("tmp/" + objeto.get("sha1"), objeto.get("repository") + "");
				
				miner.detectAtCommit(repo, String.valueOf(objeto.get("sha1")), new RefactoringHandler() {
					@Override
					public void handle(String commitId, List<Refactoring> refactorings) {
						System.out.println("Refatoraçoes no commit " + commitId);
						for (Refactoring ref : refactorings) {
							try {
								
								ExtractOperationRefactoring rea = (ExtractOperationRefactoring)ref;
								UMLOperationBodyMapper mapper = rea.getBodyMapper();
								List<StatementObject> newLeaves = mapper.getNonMappedLeavesT2(); //newly added leaf statements
								List<CompositeStatementObject> newComposites = mapper.getNonMappedInnerNodesT2(); //newly added composite statements
								List<StatementObject> deletedLeaves = mapper.getNonMappedLeavesT1(); //deleted leaf statements
								List<CompositeStatementObject> deletedComposites = mapper.getNonMappedInnerNodesT1(); //deleted composite statements
								
								toJson(ref.toJSON(), String.valueOf(objeto.get("repository")));
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}

		} catch (Exception e) {
			System.out.println(e.toString());
		}

	}

	static void toJson(String b, String title) throws IOException {
		FileWriter writer = new FileWriter(
				"/Users/marcosgenesis/eclipse-workspace/refactminer/src/main/java/refactminer/metrics.json");
		writer.write("{\n\"" + title + "\": ");
		writer.write(b);
		writer.write("\n}");
		writer.close();
	}
}
