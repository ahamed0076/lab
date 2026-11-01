
package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

import java.util.*;

public class CloudSimMulti {

    static final int VM_COUNT = 3;
    static final int CLOUDLET_COUNT = 20;

    public static void main(String[] args) {

        // Select scheduling strategy
        String strategy = "FCFS";

        if (args.length > 0) {
            strategy = args[0].toUpperCase();
        }

        boolean timeShared = strategy.equals("TIMESHARED");

        if (!timeShared && !strategy.equals("FCFS")) {
            System.out.println(
                "Use FCFS or TIMESHARED as the argument."
            );
            return;
        }

        try {

            System.out.println(
                "\nStarting CloudSim using " + strategy + " scheduling..."
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

            // 4. Create three virtual machines
            List<Vm> vmList = new ArrayList<>();

            for (int i = 0; i < VM_COUNT; i++) {

                CloudletScheduler scheduler;

                if (timeShared) {
                    scheduler = new CloudletSchedulerTimeShared();
                } else {
                    // FCFS: one cloudlet at a time per VM
                    scheduler = new CloudletSchedulerSpaceShared();
                }

                Vm vm = new Vm(
                    i,
                    brokerId,
                    1000,
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

            // 5. Create twenty cloudlets
            List<Cloudlet> cloudletList = new ArrayList<>();

            UtilizationModel utilization =
                new UtilizationModelFull();

            for (int i = 0; i < CLOUDLET_COUNT; i++) {

                // Different task lengths for comparison
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

                // Round-robin VM assignment
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

            // 8. Display cloudlet completion details
            System.out.println("\n========== CLOUDLET RESULTS ==========");

            System.out.printf(
                "%-12s %-12s %-12s %-15s %-15s%n",
                "Cloudlet ID",
                "Status",
                "VM ID",
                "Start Time",
                "Finish Time"
            );

            double[] vmCompletionTime = new double[VM_COUNT];

            double totalCompletionTime = 0;
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

                    double completionTime = finish - start;

                    vmCompletionTime[vmId] = Math.max(
                        vmCompletionTime[vmId],
                        finish
                    );

                    totalCompletionTime += completionTime;

                    makespan = Math.max(makespan, finish);

                    System.out.printf(
                        "%-12d %-12s %-12d %-15.2f %-15.2f%n",
                        cloudlet.getCloudletId(),
                        "SUCCESS",
                        vmId,
                        start,
                        finish
                    );
                }
            }

            // 9. Display performance comparison data
            System.out.println("\n========== PERFORMANCE RESULTS ==========");

            System.out.println("Scheduling Strategy: " + strategy);
            System.out.println("Total Cloudlets: " + CLOUDLET_COUNT);
            System.out.println("Successful Cloudlets: " + successful);

            System.out.printf(
                "Average Cloudlet Completion Time: %.2f seconds%n",
                successful > 0 ?
                    totalCompletionTime / successful : 0
            );

            System.out.printf(
                "Total Simulation Completion Time: %.2f seconds%n",
                makespan
            );

            System.out.println("\n========== VM COMPLETION TIMES ==========");

            for (int i = 0; i < VM_COUNT; i++) {

                System.out.printf(
                    "VM %d Completion Time: %.2f seconds%n",
                    i,
                    vmCompletionTime[i]
                );
            }

            System.out.println(
                "\nSimulation completed successfully."
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to create one physical host in a data centre
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

        // Create one physical host
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
