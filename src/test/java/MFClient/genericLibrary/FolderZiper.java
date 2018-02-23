package genericLibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FolderZiper {

	static private void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception {

		File folder = new File(srcFile);

		if (folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip);
		}
		else {
			byte[] buf = new byte[1024];
			int len;
			FileInputStream in = new FileInputStream(srcFile);
			zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
			while ((len = in.read(buf)) > 0) {
				zip.write(buf, 0, len);
			}
			in.close();
		}
	}

	static private void addFileToZipWithoutFolder(String path, String srcFile, ZipOutputStream zip) throws Exception {

		byte[] buf = new byte[1024];
		int len;

		try {
			FileInputStream in = new FileInputStream(srcFile);

			zip.putNextEntry(new ZipEntry(new File(srcFile).getName()));

			while ((len = in.read(buf)) > 0) {
				zip.write(buf, 0, len);
			}

			in.close();
		}
		catch (FileNotFoundException e) {
		}
	}

	static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
		File folder = new File(srcFolder);

		for (String fileName : folder.list()) {
			if (path.equals("")) {
				addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
			}
			else {
				addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
			}
		}
	}

	public void zipFolder(List <String> srcFolders, String destZipFile) throws Exception {
		ZipOutputStream zip = null;
		FileOutputStream fileWriter = null;

		fileWriter = new FileOutputStream(destZipFile);
		zip = new ZipOutputStream(fileWriter);

		for (String srcFolder : srcFolders) {
			if (new File(srcFolder).isDirectory())
				addFolderToZip("", srcFolder, zip);
			else
				addFileToZipWithoutFolder(new File(srcFolder).getName(), srcFolder, zip);

			zip.flush();
		}

		zip.close();
	}
}
