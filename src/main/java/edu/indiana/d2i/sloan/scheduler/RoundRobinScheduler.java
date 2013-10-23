package edu.indiana.d2i.sloan.scheduler;

import java.sql.SQLException;

import org.apache.commons.io.FilenameUtils;

import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.bean.CreateVmRequestBean;
import edu.indiana.d2i.sloan.bean.VmRequestBean;
import edu.indiana.d2i.sloan.db.DBOperations;
import edu.indiana.d2i.sloan.exception.NoResourceAvailableException;
import edu.indiana.d2i.sloan.vm.PortsPool;
import edu.indiana.d2i.sloan.vm.VMPorts;

public class RoundRobinScheduler extends Scheduler {
	private int scheduleIndex = 0;

	public RoundRobinScheduler() {

	}

	@Override
	protected VmRequestBean doSchedule(CreateVmRequestBean request)
			throws NoResourceAvailableException, SQLException {
		PortsPool portsPool = new PortsPool();
		int start = scheduleIndex;

		String workDir = FilenameUtils.concat(
				Constants.DEFAULT_VM_WORKINGDIR_PREFIX, request.getVmId());

		do {
			VMPorts vmhost = portsPool
					.nextAvailablePortPairAtHost(hosts[scheduleIndex]);
			scheduleIndex = (scheduleIndex + 1) % hosts.length;
			if (vmhost != null) {
				DBOperations.getInstance().addVM(request.getUserName(),

				request.getVmId(), request.getImageName(),
						request.getVmLoginID(), request.getVmLoginPasswd(),
						vmhost, workDir);

				return new VmRequestBean(request, vmhost.publicip,
						vmhost.sshport, vmhost.vncport, workDir);
			}
		} while (scheduleIndex != start);

		throw new NoResourceAvailableException("No port resource available.");
	}

}
