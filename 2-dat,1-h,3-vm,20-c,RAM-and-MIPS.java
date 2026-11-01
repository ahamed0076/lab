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

    // Different MIPS and RAM for each VM
    static final int[] VM_MIPS = {500, 1000, 1500};
    static final int[] VM_RAM = {512, 1024, 2048};

    public static void main(String[] args) {

        try {

            System.out.println(
                "Starting VM Resource Allocation Simulation..."
            );

            // 1. Initialize CloudSim
            CloudSim.init(1, Calendar.getInstance(), false);

            // 2. Create two data centres
            Datacenter dc0 = createDatacenter("Datacenter_0", 2);
            Datacenter dc1 = createDatacenter("Datacenter_1", 1);

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

            // Display VM resource configuration
            System.out.println("\n========== VM RESOURCE ALLOCATION ==========");

            System.out.printf(
                "%-10s %-12s %-12s %-12s%n",
                "VM ID", "MIPS", "RAM (MB)", "CPU Cores"
            );

            for (int i = 0; i < VM_COUNT; i++) {

                System.out.printf(
                    "%-10d %-12d %-12d %-12d%n",
                    i, VM_MIPS[i], VM_RAM[i], 1
                );
            }

            // 5. Create twenty cloudlets
            List<Cloudlet> cloudletList = new ArrayList<>();

            UtilizationModel utilization =
                new UtilizationModelFull();

            for (int i = 0; i < CLOUDLET_COUNT; i++) {

                Cloudlet cloudlet = new Cloudlet(
                    i,
                    20000,
                    1,
                    300,
                    300,
                    utilization,
                    utilization,
                    utilization
                );

                cloudlet.setUserId(brokerId);

                // Round-robin assignment
                cloudlet.setGuestId(i % VM_COUNT);

                cloudletList.add(cloudlet);
            }

            broker.submitCloudletList(cloudletList);

            // 6. Start simulation
            System.out.println("\nStarting CloudSim Simulation...");

            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            // 7. Get completed cloudlets
            List<Cloudlet> results =
                broker.getCloudletReceivedList();

            // 8. Display cloudlet results
            System.out.println("\n========== CLOUDLET RESULTS ==========");

            System.out.printf(
                "%-12s %-12s %-10s %-12s %-12s %-12s%n",
                "Cloudlet ID",
                "Status",
                "VM ID",
                "Start",
                "Finish",
                "Duration"
            );

            int[] vmTaskCount = new int[VM_COUNT];

            double[] vmTotalTime = new double[VM_COUNT];
            double[] vmLastFinish = new double[VM_COUNT];

            double makespan = 0;
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

                    double duration = finish - start;

                    vmTaskCount[vmId]++;

                    vmTotalTime[vmId] += duration;

                    vmLastFinish[vmId] = Math.max(
                        vmLastFinish[vmId],
                        finish
                    );

                    makespan = Math.max(makespan, finish);

                    System.out.printf(
                        "%-12d %-12s %-10d %-12.2f %-12.2f %-12.2f%n",
                        cloudlet.getCloudletId(),
                        "SUCCESS",
                        vmId,
                        start,
                        finish,
                        duration
                    );
                }
            }

            // 9. Analyze VM performance
            System.out.println("\n========== VM PERFORMANCE ANALYSIS ==========");

            System.out.printf(
                "%-10s %-12s %-12s %-12s %-15s%n",
                "VM ID",
                "MIPS",
                "RAM (MB)",
                "Tasks",
                "Avg Duration"
            );

            for (int i = 0; i < VM_COUNT; i++) {

                double averageTime = 0;

                if (vmTaskCount[i] > 0) {
                    averageTime =
                        vmTotalTime[i] / vmTaskCount[i];
                }

                System.out.printf(
                    "%-10d %-12d %-12d %-12d %-15.2f%n",
                    i,
                    VM_MIPS[i],
                    VM_RAM[i],
                    vmTaskCount[i],
                    averageTime
                );

                System.out.printf(
                    "VM %d Last Completion Time: %.2f seconds%n",
                    i,
                    vmLastFinish[i]
                );
            }

            // 10. Overall results
            System.out.println("\n========== OVERALL RESULTS ==========");

            System.out.println("Total Cloudlets: " + CLOUDLET_COUNT);

            System.out.println(
                "Successful Cloudlets: " + successful
            );

            System.out.printf(
                "Total Simulation Completion Time: %.2f seconds%n",
                makespan
            );

            System.out.println("\nSimulation completed successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to create a data centre with one physical host
    private static Datacenter createDatacenter(
            String name, int numberOfCores) {

        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();

        int mips = 2000;

        // Create physical CPU cores
        for (int i = 0; i < numberOfCores; i++) {

            peList.add(
                new Pe(new PeProvisionerSimple(mips))
            );
        }

        // Create one physical host
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
