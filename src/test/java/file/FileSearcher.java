package file;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileSearcher {
	
	public static void main(String[] args) {
		Date date = new Date();
		new FileSearcher().search("D:\\fileSearch", "qq", "", date, date);
	}
	
	public List<File> search(String rootDic, String keyWord, String suffix, Date startDate, Date endDate) {
		Filter filter = new Filter(keyWord, suffix, startDate,endDate);
		File root = new File(rootDic);
		List<File> fileList = new ArrayList<>();
		if (root.isDirectory()) {
			File[] files = root.listFiles();
			innerSearch(files, fileList, filter);
		} else if(filter.accept(root)){
			fileList.add(root);
		}
		System.out.println(fileList);
		return fileList;
	}

	public void innerSearch(File[] files, List<File> fileList, Filter filter) {
		for (File f : files) {
			if (f.isDirectory()) {
				File[] listFiles = f.listFiles();
				innerSearch(listFiles, fileList, filter);
			} else if(filter.accept(f)) {
				fileList.add(f);
			}
		}
	}
	
	class Filter implements FileFilter{
		private CharSequence keyWord;
		private Date startDate;
		private String suffix;
		private Date endDate;
		
		public Filter(CharSequence keyWord, String suffix, Date startDate, Date endDate) {
			super();
			this.keyWord = keyWord;
			this.startDate = startDate;
			this.suffix = suffix;
			this.endDate = endDate;
		}

		@Override
		public boolean accept(File file) {
			String name = file.getName();
			String fileSuffix = name.contains(".") ? name.substring(name.indexOf(".")+1) : "";
			Date modify = new Date(file.lastModified());
			if (file.getName().contains(keyWord) || fileSuffix.equalsIgnoreCase(suffix) 
//					|| modify.before(endDate) || modify.after(startDate)
					) {
				return true;
			}
			return false;
		}
		
	}
}
