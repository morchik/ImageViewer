package kz.alfa.ImageViewer.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import org.apache.commons.net.ftp.*;
import android.util.Log;

public class upLoader {
	// Now, declare a public FTP client object.

	private static final String TAG = "MyFTPClient";
	public FTPClient mFTPClient = null;

	public String u_host = "ftp.neolabshelp.16mb.com";
	public String u_user = "u379843679.u379843679tt";
	public String u_pswr = "qwe1355145";
	public int u_port = 21;
	public String u_dir = "/jpg";

	String u_srcFilePath;
	List<String> u_list;

	public boolean ftpConnect() {
		return ftpConnect(u_host, u_user, u_pswr, u_port);
	}
	// Method to connect to FTP server:
	public boolean ftpConnect(String host, String username, String password,
			int port) {
		try {
			mFTPClient = new FTPClient();
			// connecting to the host
			mFTPClient.connect(host, port);

			// now check the reply code, if positive mean connection success
			if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
				// login using username & password
				boolean status = mFTPClient.login(username, password);

				/*
				 * Set File Transfer Mode
				 * 
				 * To avoid corruption issue you must specified a correct
				 * transfer mode, such as ASCII_FILE_TYPE, BINARY_FILE_TYPE,
				 * EBCDIC_FILE_TYPE .etc. Here, I use BINARY_FILE_TYPE for
				 * transferring text, image, and compressed files.
				 */
				mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
				mFTPClient.enterLocalPassiveMode();

				return status;
			}
		} catch (Exception e) {
			Log.e(TAG, "Error: could not connect to host " + host);
		}

		return false;
	}

	// Method to disconnect from FTP server:

	public boolean ftpDisconnect() {
		try {
			mFTPClient.logout();
			mFTPClient.disconnect();
			return true;
		} catch (Exception e) {
			Log.d(TAG, "Error occurred while disconnecting from ftp server.");
		}

		return false;
	}

	// Method to get current working directory:

	public String ftpGetCurrentWorkingDirectory() {
		try {
			String workingDir = mFTPClient.printWorkingDirectory();
			Log.e(TAG, "workingDir: " + workingDir);
			return workingDir;
		} catch (Exception e) {
			Log.e(TAG, "Error: could not get current working directory.");
		}

		return null;
	}

	// Method to change working directory:
	public boolean ftpChangeDirectory(String directory_path) {
		try {
			return mFTPClient.changeWorkingDirectory(directory_path);
		} catch (Exception e) {
			Log.e(TAG, "Error: could not change directory to " + directory_path);
		}
		return false;
	}

	// Method to list all files in a directory:

	public void ftpPrintFilesList(String dir_path) {
		try {
			FTPFile[] ftpFiles = mFTPClient.listFiles(dir_path);
			int length = ftpFiles.length;

			for (int i = 0; i < length; i++) {
				String name = ftpFiles[i].getName();
				boolean isFile = ftpFiles[i].isFile();

				if (isFile) {
					Log.d(TAG, "File : " + name);
				} else {
					Log.d(TAG, "Directory : " + name);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Method to create new directory:

	public boolean ftpMakeDirectory(String new_dir_path) {
		try {
			boolean status = mFTPClient.makeDirectory(new_dir_path);
			return status;
		} catch (Exception e) {
			Log.d(TAG, "Error: could not create new directory named "
					+ new_dir_path);
		}

		return false;
	}

	// Method to delete/remove a directory:

	public boolean ftpRemoveDirectory(String dir_path) {
		try {
			boolean status = mFTPClient.removeDirectory(dir_path);
			return status;
		} catch (Exception e) {
			Log.d(TAG, "Error: could not remove directory named " + dir_path);
		}

		return false;
	}

	// Method to delete a file:

	public boolean ftpRemoveFile(String filePath) {
		try {
			boolean status = mFTPClient.deleteFile(filePath);
			return status;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	// Method to rename a file:

	public boolean ftpRenameFile(String from, String to) {
		try {
			boolean status = mFTPClient.rename(from, to);
			return status;
		} catch (Exception e) {
			Log.d(TAG, "Could not rename file: " + from + " to: " + to);
		}

		return false;
	}

	// Method to download a file from FTP server:

	/**
	 * mFTPClient: FTP client connection object (see FTP connection example)
	 * srcFilePath: path to the source file in FTP server desFilePath: path to
	 * the destination file to be saved in sdcard
	 */
	public boolean ftpDownload(String srcFilePath, String desFilePath) {
		boolean status = false;
		try {
			FileOutputStream desFileStream = new FileOutputStream(desFilePath);
			;
			status = mFTPClient.retrieveFile(srcFilePath, desFileStream);
			desFileStream.close();

			return status;
		} catch (Exception e) {
			Log.d(TAG, "download failed");
		}

		return status;
	}

	// Method to upload a file to FTP server:

	/**
	 * mFTPClient: FTP client connection object (see FTP connection example)
	 * srcFilePath: source file path in sdcard desFileName: file name to be
	 * stored in FTP server desDirectory: directory path where the file should
	 * be upload to
	 */
	public boolean ftpUpload(String srcFilePath, String desFileName,
			String desDirectory) {
		boolean status = false;
		Log.v(TAG, desFileName + " " + desDirectory);
		try {
			FileInputStream srcFileStream = new FileInputStream(srcFilePath);
			// change working directory to the destination directory
			if (ftpChangeDirectory(desDirectory)) {
				status = mFTPClient.storeFile(desFileName, srcFileStream);
			} else {
				Log.v(TAG, "try to ftpMakeDirectory " + desDirectory);
				if (ftpMakeDirs(desDirectory+"/"))
					Log.v(TAG, "ftpMakeDirectory OK");
				else
					Log.v(TAG, "ftpMakeDirectory NO");
				ftpChangeDirectory(desDirectory);
				status = mFTPClient.storeFile(desFileName, srcFileStream);
			}
			srcFileStream.close();
			return status;
		} catch (Exception e) {
			Log.d(TAG, "upload failed: " + e);
		}
		return status;
	}

	// Объект Runnable, который запускает метод для выполнения задач
	// в фоновом режиме.
	private Runnable doBackgroundThreadProcessing = new Runnable() {
		public void run() {
			backgroundUpload(u_srcFilePath); // !!!not open for each
														// file???
		}
	};

	// Объект Runnable, который запускает метод для выполнения задач
	private Runnable doListThreadProcessing = new Runnable() {
		public void run() {
			listUpload(u_list);
		}
	};

	public boolean ftpMakeDirs(String new_dir_path) {
		boolean status = true;
		ftpChangeDirectory("/");
		int pos = new_dir_path.indexOf('/', 2);
		//Log.v(TAG, "pos "+pos);
		while (pos > 0){
			String cur_d = new_dir_path.substring(0, pos);
			Log.v(TAG, pos+" "+cur_d);
			if (!ftpChangeDirectory(cur_d)){
				if (ftpMakeDirectory(cur_d))
					Log.v(TAG, pos+" ftpMakeDirectory ok "+cur_d);
				else
					Log.v(TAG, pos+" ftpMakeDirectory no "+cur_d);
			}
			if (!ftpChangeDirectory(cur_d)){
				Log.d(TAG, pos+" No sucs for "+cur_d);
				return false;
			}
			pos = new_dir_path.indexOf('/', pos + 1);
			//Log.v(TAG, "pos "+pos);
		}
		return status;
	}

	public void oneTaskUpLoad(String srcFilePath) {
		Log.d(TAG, " someTask " + srcFilePath);
		u_srcFilePath = srcFilePath;
		// Здесь трудоемкие задачи переносятся в дочерний поток.
		Thread thread = new Thread(null, doBackgroundThreadProcessing,
				"Background");
		thread.start();
	}

	public void listTaskUpLoad(List<String> list) {
		Log.d(TAG, " listTaskUpLoad size: " + list.size());
		u_list = list;
		// Здесь трудоемкие задачи переносятся в дочерний поток.
		Thread thread = new Thread(null, doListThreadProcessing,
				"BackgroundLST");
		thread.start();
	}

	public void listUpload(List<String> list) {
		mFTPClient = new FTPClient();
		try{
			mFTPClient.connect(u_host, u_port);
			mFTPClient.login(u_user, u_pswr);
		} catch (Exception e) {
			Log.e(TAG, "upload ftp connect failed: " + e);
			return;
		}
		Log.w(TAG, " u_host    " + u_host);
		// now check the reply code, if positive mean connection success
		if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
			for (int i = 0; i < list.size(); i++)
				try {
					String srcFilePath = list.get(i);
					Log.d(TAG, i + " Upload for    " + srcFilePath);
					Log.d(TAG, " isPositiveCompletion   ");
					mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
					mFTPClient.enterLocalPassiveMode();
					boolean isFile = ftpUpload(srcFilePath, (new java.io.File(
							srcFilePath)).getName(), u_dir
							/*+ (new java.io.File(srcFilePath)).getParent()*/
								);
					if (isFile) {
						Log.v(TAG, " File uploaded ... ");
						//(new Db_Helper(context)).update_status(srcFilePath);
					} else {
						Log.v(TAG, " File not uploaded!!! ");
					}
					Log.v(TAG, " ftpUpload   ");

				} catch (Exception e) {
					Log.e(TAG, "upload ftp failed: " + e);
				}
			try {
				mFTPClient.logout();
				mFTPClient.disconnect();
			} catch (Exception e) {
				Log.e(TAG, "upload ftp disconnect failed: " + e);
			}
		}
	}

	public boolean backgroundUpload(String srcFilePath) {
		mFTPClient = new FTPClient();
		// connecting to the host
		boolean status = false;
		try {
			Log.w(TAG, " backgroundUpload   " + srcFilePath);
			mFTPClient.connect(u_host, 21);
			status = mFTPClient.login(u_user, u_pswr);
			// now check the reply code, if positive mean connection success
			if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
				/*
				 * Set File Transfer Mode
				 * 
				 * To avoid corruption issue you must specified a correct
				 * transfer mode, such as ASCII_FILE_TYPE, BINARY_FILE_TYPE,
				 * EBCDIC_FILE_TYPE .etc. Here, I use BINARY_FILE_TYPE for
				 * transferring text, image, and compressed files.
				 */
				Log.v(TAG, " isPositiveCompletion   ");
				mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
				mFTPClient.enterLocalPassiveMode();
				boolean isFile = ftpUpload(srcFilePath, (new java.io.File(
						srcFilePath)).getName(), u_dir
						/*+ (new java.io.File(srcFilePath)).getParent()*/);
				if (isFile) {
					Log.v(TAG, " File uploaded ... ");
					//(new Db_Helper(context)).update_status(srcFilePath);
				} else {
					Log.v(TAG, " File not uploaded!!! ");
				}
				Log.v(TAG, " ftpUpload   ");
				mFTPClient.logout();
				mFTPClient.disconnect();
			}
		} catch (Exception e) {
			Log.e(TAG, "upload ftp failed: " + e);
		}
		return status;
	}
}
