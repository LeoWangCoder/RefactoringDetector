import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;

public class WritingFileRefactoringHandler extends RefactoringHandler {
	private Project project;
	private int counts = 0;
	
	public WritingFileRefactoringHandler(Project project) {
		this.project = project;
	}
	
	@Override
	public void handle(String commitId, List<Refactoring> refactorings, 
		  Map<String, String> fileContentsBefore, Map<String, String> fileContentsCurrent) {
		if(refactorings.size() > 0) {
			for (Refactoring ref : refactorings) {
				try {
					writeRefInfo(commitId, ref, this.project);
					writeFileContents(commitId, "before", fileContentsBefore);
					writeFileContents(commitId, "current", fileContentsCurrent);
					if (++counts % 10 == 0) {
						System.out.println("Project: " + this.project.getName() + ", " + counts + " refactorings detected");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void writeRefInfo(String commitId, Refactoring ref, Project project) throws IOException {
		// file name format: data/{refactoring name}/{project name}/{ID}.json
		StringBuilder refFileName = new StringBuilder();
		refFileName.append("data/ref_infos/").append(ref.getName().replaceAll(" ", "_")).append("/");
		refFileName.append(project.getName()).append("/").append(UUID.randomUUID().toString()).append(".json");
		
		write(refFileName.toString(), insertCommitIdToJson(ref.toJSON(), commitId));
	}
	
	private void writeFileContents(String commitId, String flag, Map<String, String> fileContents) throws IOException {
		for (Map.Entry<String, String> pair : fileContents.entrySet()) {
			String key = pair.getKey();
			StringBuilder filename = new StringBuilder();
			filename.append("data/src_code/").append(commitId).append("/").append(key.substring(0, key.lastIndexOf('/') + 1));
			filename.append(flag).append("_").append(key.substring(key.lastIndexOf('/') + 1));
			write(filename.toString(), pair.getValue());
		}
	}
	
	private String insertCommitIdToJson(String refJSON, String commitId) {
		JSONObject jObj = new JSONObject(refJSON);
		jObj.put("commitId", commitId);
		return jObj.toString();
	}
	
	private void write(String path, String content) throws IOException {
		File f = new File(path);
		f.getParentFile().mkdirs();
		FileWriter writer  = new FileWriter(f);
		writer.write(content);
		writer.flush();
		writer.close();
	}
}
