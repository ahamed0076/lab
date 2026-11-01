//mvn -pl modules/cloudsim-examples/ -am compile -DskipTests
//mvn exec:java -pl modules/cloudsim-examples/ "-Dexec.mainClass=org.cloudbus.cloudsim.examples.CloudSimMulti"
package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

import java.util.*;

public class CloudSimMulti {

    static final int VM_COUNT = 3;
    static final int CLOUDLET_COUNT = 20;

    static final int VM_MIPS = 1000;
    static final long CLOUDLET_LENGTH = 40000;

    public static void main(String[] args) {

        try {

            System.out.println(
                "\n===== TIME SHARED SCHEDULING ====="
            );

            double timeSharedTime =
                runSimulation(true);

            System.out.println(
                "\n===== SPACE SHARED SCHEDULING ====="
            );

            double spaceSharedTime =
                runSimulation(false);

            System.out.println(
                "\n===== SCHEDULING COMPARISON ====="
            );

            System.out.printf(
                "Time Shared Execution Time: %.2f seconds%n",
                timeSharedTime
            );

            System.out.printf(
                "Space Shared Execution Time: %.2f seconds%n",
                spaceSharedTime
            );

            if (timeSharedTime < spaceSharedTime) {
                System.out.println(
                    "Time Shared is faster for this workload."
                );
            } else if (spaceSharedTime < timeSharedTime) {
                System.out.println(
                    "Space Shared is faster for this workload."
                );
            } else {
                System.out.println(
                    "Both strategies have equal execution time."
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Run simulation using the selected scheduling strategy
    private static double runSimulation(boolean timeShared)
            throws Exception {

        // Initialize CloudSim for each independent simulation
        CloudSim.init(1, Calendar.getInstance(), false);

        // Create two data centres
        createDatacenter("Datacenter_0", 2);
        createDatacenter("Datacenter_1", 1);

        // Create broker
        DatacenterBroker broker =
            new DatacenterBroker("Broker");

        int brokerId = broker.getId();

        // Create three VMs
        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < VM_COUNT; i++) {

            CloudletScheduler scheduler;

            if (timeShared) {
                scheduler = new CloudletSchedulerTimeShared();
            } else {
                scheduler = new CloudletSchedulerSpaceShared();
            }

            Vm vm = new Vm(
                i,
                brokerId,
                VM_MIPS,
                1,
                512,
                1000,
                10000,
                "Xen",
                scheduler
            );

            vmList.add(vm);
        }

        broker.submitGuestList(vmList);

        // Create 20 cloudlets
        List<Cloudlet> cloudletList = new ArrayList<>();

        UtilizationModel utilization =
            new UtilizationModelFull();

        for (int i = 0; i < CLOUDLET_COUNT; i++) {

            Cloudlet cloudlet = new Cloudlet(
                i,
                CLOUDLET_LENGTH,
                1,
                300,
                300,
                utilization,
                utilization,
                utilization
            );

            cloudlet.setUserId(brokerId);

            // Round-robin cloudlet assignment
            cloudlet.setGuestId(i % VM_COUNT);

            cloudletList.add(cloudlet);
        }

        broker.submitCloudletList(cloudletList);

        // Start simulation
        CloudSim.startSimulation();

        CloudSim.stopSimulation();

        // Retrieve results
        List<Cloudlet> results =
            broker.getCloudletReceivedList();

        // Display individual cloudlet results
        System.out.printf(
            "%-12s %-12s %-12s %-12s %-12s%n",
            "Cloudlet ID",
            "Status",
            "VM ID",
            "Start",
            "Finish"
        );

        double[] vmFinishTime = new double[VM_COUNT];

        int successful = 0;

        for (Cloudlet cloudlet : results) {

            if (cloudlet.getStatus() ==
                    Cloudlet.CloudletStatus.SUCCESS) {

                successful++;

                int vmId = (int) cloudlet.getGuestId();

                double finish =
                    cloudlet.getExecFinishTime();

                vmFinishTime[vmId] = Math.max(
                    vmFinishTime[vmId], finish
                );

                System.out.printf(
                    "%-12d %-12s %-12d %-12.2f %-12.2f%n",
                    cloudlet.getCloudletId(),
                    "SUCCESS",
                    vmId,
                    cloudlet.getExecStartTime(),
                    finish
                );
            }
        }

        // Calculate VM execution times
        System.out.println("\nVM EXECUTION TIME");

        double totalExecutionTime = 0;

        for (int i = 0; i < VM_COUNT; i++) {

            System.out.printf(
                "VM %d Execution Time: %.2f seconds%n",
                i,
                vmFinishTime[i]
            );

            totalExecutionTime = Math.max(
                totalExecutionTime,
                vmFinishTime[i]
            );
        }

        System.out.println(
            "Successful Cloudlets: " + successful
        );

        System.out.printf(
            "Total Simulation Execution Time: %.2f seconds%n",
            totalExecutionTime
        );

        return totalExecutionTime;
    }

    // Create a data centre with one physical host
    private static Datacenter createDatacenter(
            String name, int numberOfCores) {

        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();

        int mips = 1000;

        // Create CPU cores
        for (int i = 0; i < numberOfCores; i++) {
            peList.add(
                new Pe(new PeProvisionerSimple(mips))
            );
        }

        // Create physical host
        int ram = 4096;
        long storage = 1000000;
        int bw = 10000;

        Host host = new Host(
            new RamProvisionerSimple(ram),
            new BwProvisionerSimple(bw),
            storage,
            peList,
            new VmSchedulerTimeShared(peList)
        );

        hostList.add(host);

        // Data centre characteristics
        DatacenterCharacteristics characteristics =
            new DatacenterCharacteristics(
                "x86",
                "Linux",
                "Xen",
                hostList,
                10.0,
                3.0,
                0.05,
                0.001,
                0.0
            );

        try {

            return new Datacenter(
                name,
                characteristics,
                new VmAllocationPolicySimple(hostList),
                new LinkedList<Storage>(),
                0
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
