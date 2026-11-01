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

    // Different VM processing capacities
    static final int[] VM_MIPS = {500, 1000, 2000};
    static final int[] VM_RAM = {512, 1024, 2048};

    public static void main(String[] args) {

        try {

            System.out.println(
                "Starting VM Performance Comparison..."
            );

            // 1. Initialize CloudSim
            CloudSim.init(1, Calendar.getInstance(), false);

            // 2. Create two data centres
            createDatacenter("Datacenter_0", 2);
            createDatacenter("Datacenter_1", 1);

            // 3. Create broker
            DatacenterBroker broker =
                new DatacenterBroker("Broker");

            int brokerId = broker.getId();

            // 4. Create three VMs
            List<Vm> vmList = new ArrayList<>();

            for (int i = 0; i < VM_COUNT; i++) {

                Vm vm = new Vm(
                    i,
                    brokerId,
                    VM_MIPS[i],
                    1,
                    VM_RAM[i],
                    1000,
                    10000,
                    "Xen",
                    new CloudletSchedulerTimeShared()
                );

                vmList.add(vm);
            }

            broker.submitGuestList(vmList);

            // Display VM configuration
            System.out.println("\n========== VM CONFIGURATION ==========");

            for (int i = 0; i < VM_COUNT; i++) {

                System.out.println(
                    "VM " + i +
                    " | MIPS: " + VM_MIPS[i] +
                    " | RAM: " + VM_RAM[i] + " MB"
                );
            }

            // 5. Create twenty cloudlets
            List<Cloudlet> cloudletList = new ArrayList<>();

            UtilizationModel utilization =
                new UtilizationModelFull();

            for (int i = 0; i < CLOUDLET_COUNT; i++) {

                // Different workload lengths
                long length = 10000 + (i % 5) * 10000;

                Cloudlet cloudlet = new Cloudlet(
                    i,
                    length,
                    1,
                    300,
                    300,
                    utilization,
                    utilization,
                    utilization
                );

                cloudlet.setUserId(brokerId);

                // Assign cloudlets to VMs in round-robin order
                cloudlet.setGuestId(i % VM_COUNT);

                cloudletList.add(cloudlet);
            }

            broker.submitCloudletList(cloudletList);

            // 6. Start simulation
            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            // 7. Retrieve completed cloudlets
            List<Cloudlet> results =
                broker.getCloudletReceivedList();

            // 8. Display cloudlet execution details
            System.out.println("\n========== CLOUDLET EXECUTION RESULTS ==========");

            System.out.printf(
                "%-12s %-10s %-10s %-12s %-12s %-12s%n",
                "Cloudlet ID",
                "VM ID",
                "Status",
                "Start Time",
                "Finish Time",
                "Exec Time"
            );

            int[] vmTaskCount = new int[VM_COUNT];

            double[] vmFirstStart = new double[VM_COUNT];
            double[] vmLastFinish = new double[VM_COUNT];
            double[] vmTotalCpuTime = new double[VM_COUNT];

            Arrays.fill(vmFirstStart, Double.MAX_VALUE);

            int successful = 0;

            for (Cloudlet cloudlet : results) {

                if (cloudlet.getStatus() ==
                        Cloudlet.CloudletStatus.SUCCESS) {

                    successful++;

                    int vmId = (int) cloudlet.getGuestId();

                    double start =
                        cloudlet.getExecStartTime();

                    double finish =
                        cloudlet.getExecFinishTime();

                    double executionTime =
                        cloudlet.getActualCPUTime();

                    vmTaskCount[vmId]++;

                    vmFirstStart[vmId] = Math.min(
                        vmFirstStart[vmId], start
                    );

                    vmLastFinish[vmId] = Math.max(
                        vmLastFinish[vmId], finish
                    );

                    vmTotalCpuTime[vmId] += executionTime;

                    System.out.printf(
                        "%-12d %-10d %-10s %-12.2f %-12.2f %-12.2f%n",
                        cloudlet.getCloudletId(),
                        vmId,
                        "SUCCESS",
                        start,
                        finish,
                        executionTime
                    );
                }
            }

            // 9. Compare VM performance
            System.out.println("\n========== VM PERFORMANCE COMPARISON ==========");

            System.out.printf(
                "%-10s %-10s %-12s %-12s %-12s %-12s%n",
                "VM ID",
                "MIPS",
                "Tasks",
                "Start Time",
                "Finish Time",
                "Total CPU"
            );

            for (int i = 0; i < VM_COUNT; i++) {

                double start = vmTaskCount[i] > 0
                    ? vmFirstStart[i] : 0;

                System.out.printf(
                    "%-10d %-10d %-12d %-12.2f %-12.2f %-12.2f%n",
                    i,
                    VM_MIPS[i],
                    vmTaskCount[i],
                    start,
                    vmLastFinish[i],
                    vmTotalCpuTime[i]
                );
            }

            // 10. Overall results
            System.out.println("\n========== OVERALL RESULTS ==========");

            System.out.println("Total Cloudlets: " + CLOUDLET_COUNT);

            System.out.println(
                "Successful Cloudlets: " + successful
            );

            System.out.println(
                "Cloudlet assignment: Round Robin"
            );

            double totalCompletion = 0;

            for (int i = 0; i < VM_COUNT; i++) {
                totalCompletion = Math.max(
                    totalCompletion,
                    vmLastFinish[i]
                );
            }

            System.out.printf(
                "Total Simulation Completion Time: %.2f seconds%n",
                totalCompletion
            );

            System.out.println(
                "\nSimulation completed successfully."
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Create a data centre with one physical host
    private static Datacenter createDatacenter(
            String name, int numberOfCores) {

        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();

        int mips = 4000;

        // Create physical CPU cores
        for (int i = 0; i < numberOfCores; i++) {
            peList.add(
                new Pe(new PeProvisionerSimple(mips))
            );
        }

        // Create physical host
        int ram = 8192;
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
