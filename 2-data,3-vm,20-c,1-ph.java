//mvn -pl modules/cloudsim-examples/ -am compile -DskipTests
//mvn exec:java -pl modules/cloudsim-examples/ "-Dexec.mainClass=org.cloudbus.cloudsim.examples.CloudSimMulti"
package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class CloudSimMulti {

    private static List<Vm> vmList;
    private static List<Cloudlet> cloudletList;

    public static void main(String[] args) {

        try {
            Log.println("Starting CloudSim Multi-Datacenter Simulation...");

            // 1. Initialize CloudSim
            CloudSim.init(1, Calendar.getInstance(), false);

            // 2. Create two data centres
            Datacenter dc0 = createDatacenter("Datacenter_0", 2);
            Datacenter dc1 = createDatacenter("Datacenter_1", 1);

            // 3. Create broker
            DatacenterBroker broker =
                    new DatacenterBroker("Broker");

            int brokerId = broker.getId();

            // 4. Create three virtual machines
            vmList = new ArrayList<>();

            int mips = 1000;
            int ram = 512;
            long bw = 1000;
            long size = 10000;
            int pesNumber = 1;
            String vmm = "Xen";

            for (int i = 0; i < 3; i++) {

                Vm vm = new Vm(
                        i,
                        brokerId,
                        mips,
                        pesNumber,
                        ram,
                        bw,
                        size,
                        vmm,
                        new CloudletSchedulerTimeShared()
                );

                vmList.add(vm);
            }

            // Submit VMs to broker
            broker.submitGuestList(vmList);

            // 5. Create twenty cloudlets
            cloudletList = new ArrayList<>();

            long length = 10000;
            long fileSize = 300;
            long outputSize = 300;

            UtilizationModel utilization =
                    new UtilizationModelFull();

            for (int i = 0; i < 20; i++) {

                Cloudlet cloudlet = new Cloudlet(
                        i,
                        length,
                        1,
                        fileSize,
                        outputSize,
                        utilization,
                        utilization,
                        utilization
                );

                cloudlet.setUserId(brokerId);

                // Round-robin assignment
                cloudlet.setGuestId(i % 3);

                cloudletList.add(cloudlet);
            }

            // Submit cloudlets to broker
            broker.submitCloudletList(cloudletList);

            // 6. Start simulation
            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            // 7. Get completed cloudlets
            List<Cloudlet> resultList =
                    broker.getCloudletReceivedList();

            // 8. Display cloudlet results
            printCloudletList(resultList);

            // 9. Calculate execution time and CPU utilization
            calculatePerformance(
                    resultList,
                    dc0.getId(),
                    dc1.getId()
            );

            Log.println("\nCloudSim simulation completed!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to create a data centre with one physical host
    private static Datacenter createDatacenter(
            String name, int numberOfCores) {

        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();

        int mips = 1000;

        // Create physical CPU cores
        for (int i = 0; i < numberOfCores; i++) {
            peList.add(
                    new Pe(new PeProvisionerSimple(mips))
            );
        }

        // Create one physical host
        int ram = 2048;
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
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";

        double timeZone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;

        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics =
                new DatacenterCharacteristics(
                        arch,
                        os,
                        vmm,
                        hostList,
                        timeZone,
                        cost,
                        costPerMem,
                        costPerStorage,
                        costPerBw
                );

        try {
            return new Datacenter(
                    name,
                    characteristics,
                    new VmAllocationPolicySimple(hostList),
                    storageList,
                    0
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Method to display cloudlet details
    private static void printCloudletList(List<Cloudlet> list) {

        DecimalFormat dft = new DecimalFormat("###.##");

        System.out.println("\n========== CLOUDLET OUTPUT ==========");

        System.out.printf(
                "%-12s %-12s %-15s %-10s %-12s %-12s%n",
                "Cloudlet ID",
                "Status",
                "Datacenter ID",
                "VM ID",
                "Start Time",
                "Finish Time"
        );

        for (Cloudlet cloudlet : list) {

            if (cloudlet.getStatus() ==
                    Cloudlet.CloudletStatus.SUCCESS) {

                System.out.printf(
                        "%-12d %-12s %-15d %-10d %-12s %-12s%n",
                        cloudlet.getCloudletId(),
                        "SUCCESS",
                        cloudlet.getResourceId(),
                        cloudlet.getGuestId(),
                        dft.format(cloudlet.getExecStartTime()),
                        dft.format(cloudlet.getExecFinishTime())
                );
            }
        }
    }

    // Method to calculate execution time and utilization
    private static void calculatePerformance(
            List<Cloudlet> list,
            int dc0Id,
            int dc1Id) {

        double makespan = 0;
        double totalCpuTime = 0;

        double[] vmCpuTime = new double[3];
        double[] dcCpuTime = new double[2];

        int successful = 0;

        for (Cloudlet cloudlet : list) {

            if (cloudlet.getStatus() ==
                    Cloudlet.CloudletStatus.SUCCESS) {

                successful++;

                double cpuTime = cloudlet.getActualCPUTime();

                totalCpuTime += cpuTime;

                makespan = Math.max(
                        makespan,
                        cloudlet.getExecFinishTime()
                );

                int vmId = cloudlet.getGuestId();

                if (vmId >= 0 && vmId < 3) {
                    vmCpuTime[vmId] += cpuTime;
                }

                int dcId = cloudlet.getResourceId();

                if (dcId == dc0Id) {
                    dcCpuTime[0] += cpuTime;
                } else if (dcId == dc1Id) {
                    dcCpuTime[1] += cpuTime;
                }
            }
        }

        System.out.println("\n========== PERFORMANCE RESULTS ==========");

        System.out.println("Total Cloudlets: " + list.size());
        System.out.println("Successful Cloudlets: " + successful);

        System.out.printf(
                "Total Execution Time: %.2f seconds%n",
                makespan
        );

        System.out.println("\n========== CPU RESOURCE UTILIZATION ==========");

        // Overall utilization: 3 VM CPU cores
        if (makespan > 0) {

            double overallUtilization =
                    (totalCpuTime / (makespan * 3)) * 100;

            System.out.printf(
                    "Overall CPU Utilization: %.2f%%%n",
                    overallUtilization
            );
        }

        // Individual VM utilization
        for (int i = 0; i < 3; i++) {

            double utilization = 0;

            if (makespan > 0) {
                utilization =
                        (vmCpuTime[i] / makespan) * 100;
            }

            System.out.printf(
                    "VM %d Utilization: %.2f%%%n",
                    i,
                    utilization
            );
        }

        // Data centre utilization
        int[] dcCores = {2, 1};

        for (int i = 0; i < 2; i++) {

            double utilization = 0;

            if (makespan > 0) {
                utilization =
                        (dcCpuTime[i] /
                        (makespan * dcCores[i])) * 100;
            }

            System.out.printf(
                    "Datacenter %d Utilization: %.2f%%%n",
                    i,
                    utilization
            );
        }
    }
}
