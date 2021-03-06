/*******************************************************************************
 * Copyright 2014 The Trustees of Indiana University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.indiana.d2i.sloan.db;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.*;

import edu.indiana.d2i.sloan.Configuration;
import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.bean.CreateVmRequestBean;
import edu.indiana.d2i.sloan.bean.ResultBean;
import edu.indiana.d2i.sloan.bean.VmInfoBean;
import edu.indiana.d2i.sloan.exception.NoItemIsFoundInDBException;
import edu.indiana.d2i.sloan.vm.VMMode;
import edu.indiana.d2i.sloan.vm.VMPorts;
import edu.indiana.d2i.sloan.vm.VMState;

@Ignore
public class TestDBOperations {
	private int[] portsUsed = null;
	private void loadDataToImageTable(int records) throws SQLException {
		Connection connection = null;
		PreparedStatement pst = null;
		
		try {
			String insertTableSQL = "INSERT INTO images"
				+ "(imagename, imagepath, imagedescription, loginusername, loginpassword) " +
				"VALUES (?, ?, ?, ?, ?)";
			connection = DBConnections.getInstance().getConnection();
			
			int count = records;
			portsUsed = new int[count * 2];
			for (int i = 0; i < count; i++) {
				pst = connection.prepareStatement(insertTableSQL);
				pst.setString(1, "imagename-" + i);
				pst.setString(2, "/var/instance/imagename-" + i);
				pst.setString(3, "This is " + i + " image");
				pst.setString(4, "user" + i);
				pst.setString(5, "pwd" + i);
				pst.executeUpdate();
				pst.close();
			}
		} finally {
			if (pst != null) pst.close();
			if (connection != null) connection.close();
		}
	}
	
	private void loadDataToVmTable(int records) throws SQLException {
		loadDataToImageTable(records);
		
		Connection connection = null;
		PreparedStatement pst = null;
		
		try {
			String insertTableSQL = "INSERT INTO vms"
				+ "(vmid, vmmode, vmstate, publicip, sshport, vncport, workingdir, imagename, vncusername, vncpassword) VALUES"
				+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			connection = DBConnections.getInstance().getConnection();
			
			int count = records;
			portsUsed = new int[count * 2];
			for (int i = 0; i < count; i++) {
				pst = connection.prepareStatement(insertTableSQL);
				pst.setString(1, "vmid-" + i);
				pst.setString(2, VMMode.MAINTENANCE.toString());
				pst.setString(3, VMState.RUNNING.toString());
				pst.setString(4, "192.168.0." + (i+2));
				pst.setInt(5, 2000 + i*2);
				pst.setInt(6, 2000 + i*2 + 1);
				pst.setString(7, "/var/instance/" + "vmid-" + i);
				pst.setString(8, "imagename-" + i);
				pst.setString(9, "username-" + i);
				pst.setString(10, "password");
				pst.executeUpdate();
				pst.close();
				
				portsUsed[i * 2] = 2000 + i*2;
				portsUsed[i * 2 + 1] = 2000 + i*2 + 1;
			}
		} finally {
			if (pst != null) pst.close();
			if (connection != null) connection.close();
		}
	}
	
	private void loadDataToUserTable(int records) throws SQLException {
		Connection connection = null;
		PreparedStatement pst = null;
		
		try {
			String insertTableSQL = "INSERT INTO users" +
				"(username, useremail, cpuleftquota, memoryleftquota, diskleftquota) VALUES" + 
				"(?, ?, ?, ?, ?)";
			connection = DBConnections.getInstance().getConnection();
			
			int count = records;
			for (int i = 0; i < count; i++) {
				pst = connection.prepareStatement(insertTableSQL);
				pst.setString(1, "username-" + i);
				pst.setString(2, "username-" + i + "@gmail.com");
				pst.setInt(3, Integer.valueOf(Constants.DEFAULT_USER_CPU_QUOTA_IN_NUM)); // vcpus
				pst.setInt(4, Integer.valueOf(Constants.DEFAULT_USER_MEMORY_QUOTA_IN_MB)); // memory size in MB
				pst.setInt(5, 
					Integer.valueOf(Constants.DEFAULT_USER_DISK_QUOTA_IN_GB)); // volume size in GB
				
				pst.executeUpdate();
				pst.close();
			}
		} finally {
			if (pst != null) pst.close();
			if (connection != null) connection.close();
		}
	}
	
	@BeforeClass
	public static void beforeClass() {
		Configuration.getInstance().setProperty(
				Configuration.PropertyName.DB_DRIVER_CLASS, "com.mysql.jdbc.Driver");
			Configuration.getInstance().setProperty(
				Configuration.PropertyName.JDBC_URL, "jdbc:mysql://localhost:3306/" + DBSchema.DB_NAME);
			Configuration.getInstance().setProperty(
					Configuration.PropertyName.DB_USER, "root");
			Configuration.getInstance().setProperty(
					Configuration.PropertyName.DB_PWD, "root");
	}
	
	@AfterClass
	public static void afterClass() {
		DBOperations.getInstance().close();
	}
	
	@Before
	public void before() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "root");
		ScriptRunner script = new ScriptRunner(connection, false, false);
		script.runScript(new java.io.FileReader("src/main/resources/createtables.sql"));
		connection.close();
	}
	
	@Test
	public void testAddGetAndDeleteVM() throws SQLException, NoItemIsFoundInDBException {
		String userName, vmid, workDir;
		
		int count = 4;
		int imageCnt = 2;
		loadDataToUserTable(count);	
		loadDataToImageTable(imageCnt);
		
		int[] portsExpected = new int[count * 2];
		List<String> vmids = new ArrayList<String>();
		List<String> userNames = new ArrayList<String>();
		
		for (int index = 0; index < count; index++) {
			userName = "username-" + index;
			if (index == count-1) {
				userName = "username-" + (index-1);
			}
			
			vmid = "vmid-" + index;
			VMPorts host = new VMPorts("192.168.0." + (index+2), 2000 + index*2, 2000 + index*2 + 1);
			workDir = "/var/instance/" + "vmid-" + index;
			vmids.add(vmid);
			userNames.add(userName);
			
			portsExpected[index*2] = 2000 + index*2;
			portsExpected[index*2+1] = 2000 + index*2 + 1;
			DBOperations.getInstance().addVM(userName, vmid, "imagename-"+(index%imageCnt), 
				"vncusername", "vncpassword", host, workDir, 2, 1024, 10);
		}
		
		// trigger error
		try {
			userName = "user-" + 0;
			vmid = "vmid-" + 0;
			VMPorts host = new VMPorts("192.168.0." + (0+2), 2000 + 0*2, 2000 + 0*2 + 1);
			workDir = "/var/instance/" + "vmid-" + 0;
			DBOperations.getInstance().addVM(userName, vmid, "imagename-0", "vmusername", "vmpasswd", 
					host, workDir, 2, 1024, 10);
		} catch (SQLException e) {
			// nothing
		}
		
		
		// read 1 vm back
		VmInfoBean vmInfo = DBOperations.getInstance().getVmInfo("username-" + 0, "vmid-" + 0);
		Assert.assertEquals(VMState.CREATE_PENDING, vmInfo.getVmstate());
		Assert.assertEquals(2000, vmInfo.getSshport());
		Assert.assertEquals(2001, vmInfo.getVncport());
		Assert.assertEquals("192.168.0.2", vmInfo.getPublicip());
		Assert.assertEquals("vmid-" + 0, vmInfo.getVmid());
		Assert.assertEquals(VMMode.NOT_DEFINED, vmInfo.getVmmode());
		Assert.assertEquals("user"+0, vmInfo.getVmLoginId());
		Assert.assertEquals("pwd"+0, vmInfo.getVmLoginPwd());
		Assert.assertEquals("vncusername", vmInfo.getVNCloginId());
		Assert.assertEquals("vncpassword", vmInfo.getVNCloginPwd());
		
		// read 2 vm back
		Assert.assertTrue(DBOperations.getInstance().getVmInfo("username-" + (count-2)).size() == 2);
		
		// read ports in use
		List<VmInfoBean> vmStatus = DBOperations.getInstance().getExistingVmInfo();
		int[] ports = new int[vmStatus.size() * 2];
		for (int i = 0; i < vmStatus.size(); i++) {
			ports[i*2] = vmStatus.get(i).getSshport();
			ports[i*2+1] = vmStatus.get(i).getVncport();
		}
		Arrays.sort(portsExpected);
		Arrays.sort(ports);
		Assert.assertArrayEquals(portsExpected, ports);
		
		// delete vm
		for (int i = 0; i < vmids.size(); i++) {
			VmInfoBean vinfo = new VmInfoBean(vmids.get(i), null, null,
				null, null, 0, 0, 2, 1024, 10, null, null, null, null, 
				null, null, null, null, null); 
			DBOperations.getInstance().deleteVMs(userNames.get(i), vinfo);
		}		
		
		vmStatus = DBOperations.getInstance().getExistingVmInfo();
		Assert.assertEquals(0, vmStatus.size());		
	}
	
	@Test
	public void testInsertUserIfNotExists() throws SQLException {
		DBOperations.getInstance().insertUserIfNotExists("myusername-0", "myusername-0@gmail.com");
		DBOperations.getInstance().insertUserIfNotExists("myusername-0", "myusername-0@gmail.com");
	}
	
	@Test
	public void testUpdateVMState() throws SQLException {
		int count = 3;
		loadDataToVmTable(count);
		
		for (int i = 0; i < count; i++) {
			String vmid = "vmid-" + i;
			DBOperations.getInstance().updateVMState(vmid, VMState.SHUTDOWN);
		}
		
		List<VmInfoBean> vmStatus = DBOperations.getInstance().getExistingVmInfo();
		Assert.assertEquals(count, vmStatus.size());
		for (int i = 0; i < count; i++) {
			Assert.assertEquals(VMState.SHUTDOWN, vmStatus.get(i).getVmstate());
		}
	}
	
	@Test
	public void testGetImagePath() throws SQLException {
		int count = 3;
		loadDataToImageTable(count);
		for (int i = 0; i < count; i++) {
			String imagePath = DBOperations.getInstance().getImagePath("imagename-" + i);
			Assert.assertEquals("/var/instance/imagename-" + i, imagePath);
		}
	}
	
	@Test 
	public void testQuotaExceedsLimit() throws SQLException, NoItemIsFoundInDBException {
		int count = 3;
		loadDataToUserTable(count);
		
		CreateVmRequestBean request = new CreateVmRequestBean("username-0",
				null, null, null, null, 
				Integer.valueOf(Constants.DEFAULT_USER_MEMORY_QUOTA_IN_MB),
				Integer.valueOf(Constants.DEFAULT_USER_CPU_QUOTA_IN_NUM) - 1,
				Integer.valueOf(Constants.DEFAULT_USER_DISK_QUOTA_IN_GB) - 1, 
				"/path/to/work/dir");
		
		Assert.assertTrue(DBOperations.getInstance().quotasNotExceedLimit(request));
		Assert.assertFalse(DBOperations.getInstance().quotasNotExceedLimit(request));
		
		VmInfoBean vmInfo = new VmInfoBean("vmid-0", null, null, null, null, 
			2000, 2001, request.getVcpu(), request.getMemory(), request.getVolumeSizeInGB(), 
			null, null, null, null, null, null, null, null, null);
		DBOperations.getInstance().deleteVMs("username-0", vmInfo);
		
		Assert.assertTrue(DBOperations.getInstance().quotasNotExceedLimit(request));
	}
	
	@Test
	public void testUpdateVMModeVMState() throws SQLException {
		int records = 4;
		loadDataToVmTable(records);
		
		DBOperations.getInstance().updateVMState("vmid-0", VMState.RUNNING);
		DBOperations.getInstance().updateVMMode("vmid-0", VMMode.MAINTENANCE);
		DBOperations.getInstance().updateVMState("vmid-2", VMState.DELETE_PENDING);
		
		boolean vm1 = false;
		boolean vm2 = false;
		List<VmInfoBean> vmInfos = DBOperations.getInstance().getExistingVmInfo();
		Assert.assertEquals(4, vmInfos.size());
		for (VmInfoBean vmInfo : vmInfos) {
			if (vmInfo.getVmid().equals("vmid-0")) {
				Assert.assertEquals(VMState.RUNNING, vmInfo.getVmstate());
				Assert.assertEquals(VMMode.MAINTENANCE, vmInfo.getVmmode());
				vm1 = true;
			} else if (vmInfo.getVmid().equals("vmid-2")) {
				Assert.assertEquals(VMState.DELETE_PENDING, vmInfo.getVmstate());
				vm2 = true;
			}
		}
		Assert.assertTrue(vm1 && vm2);
	}
	
	@Test 
	public void testPutAndGetResult() throws Exception {
		loadDataToImageTable(3);
		loadDataToUserTable(3);		
		
		// add vm to uservm table
		int count = 3;
		String userName = "username-0";
		for (int index = 0; index < count; index++) {			
			String vmid = "vmid-" + index;
			VMPorts host = new VMPorts("192.168.0." + (index+2), 2000 + index*2, 2000 + index*2 + 1);
			String workDir = "/var/instance/" + "vmid-" + index;
			DBOperations.getInstance().addVM(userName, vmid, "imagename-0", 
				"vncusername", "vncpassword", host, workDir, 2, 1024, 10);
		}

		// generate file
		FileOutputStream output = new FileOutputStream("./tmpfile");
		byte[] buf = new byte[1024]; // 1 KB
		int factor = 1024; // factor * 512 Bytes = 1 MB
		Arrays.fill(buf, (byte)1);
		for (int i = 0; i < factor; i++) output.write(buf); 
		output.close();
		
		// write result for same vm, different random id
		List<String> randomids = new ArrayList<String>();
		String vmid = "vmid-0";
		for (int i = 0; i < 3; i++) {
			String randomid = UUID.randomUUID().toString();
			FileInputStream input = new FileInputStream("./tmpfile");
			DBOperations.getInstance().insertResult(vmid, randomid, input);
			randomids.add(randomid);
		}
		
		// delete vm
		for (int i = 0; i < 3; i++) {
			VmInfoBean vinfo = new VmInfoBean("vmid-"+i, null, null,
					null, null, 0, 0, 2, 1024, 10, null, null, null, null, 
					null, null, null, null, null); 
			DBOperations.getInstance().deleteVMs(userName, vinfo);
		}
		
		// read result from db
		long currentT = new java.util.Date().getTime();
		System.out.println(currentT);
		
		for (String randomid : randomids) {
			ResultBean result = DBOperations.getInstance().getResult(randomid);
			InputStream input = result.getInputstream();
			byte[] b = new byte[1024];
			while (input.read(b) != -1) {
				Assert.assertArrayEquals(buf, b);
			}
			
			System.out.println(result.getStartdate().getTime());
			
			// mark result as notified
			DBOperations.getInstance().updateResultAsNotified(randomid);
		}
		Assert.assertEquals(0, DBOperations.getInstance().getResultsUnnotified().size());
		
		// remove tmp file
		org.apache.commons.io.FileUtils.deleteQuietly(new java.io.File("./tmpfile"));
	}
}
