import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

abstract class Scheduler
{
    // Other variables
    List<Process> waitingProcesses = new ArrayList<Process>(),
        runningProcesses           = new ArrayList<Process>(), // Indexed by CPU #
        finishedProcesses          = new ArrayList<Process>();
    List<Integer> waitingTimes     = new ArrayList<Integer>(); // Indexed by CPU #
    Integer time = 0, waitingTime = 0, maxTime = 0, averageTime = 0, minTime = 0;

    public Scheduler()
    {
        for(Integer i = 0; i < Main.CPU_COUNT; ++i)
        {
            runningProcesses.add(null);
            waitingTimes.add(0);
        }
    }

    public void addProcess(Process process)
    {
        waitingProcesses.add(new Process(process));
    }

    public void addProcesses(Collection<Process> processes)
    {
        for(Process process : processes)
        {
            addProcess(process);
        }
    }

    public Boolean hasWaitingProcesses()
    {
        return waitingProcesses.size() > 0;
    }

    public Boolean hasRunningProcesses()
    {
        for(Process process : runningProcesses)
        {
            if(process != null)
            {
                return true;
            }
        }
        return false;
    }

    public Boolean hasUnfinishedProcesses()
    {
        return hasWaitingProcesses() || hasRunningProcesses();
    }
    abstract Process getNextProcess(Integer processor);
    abstract String getName();
    public void tick()
    {
        for(Integer i = 0; i < Main.CPU_COUNT; ++i)
        {
            Integer waitingTime = waitingTimes.get(i);
            Process currentProcess = runningProcesses.get(i);
            if(waitingTime > 0)
            {
                waitingTimes.set(i, --waitingTime);
                currentProcess.pause();
                if(waitingTime == 0)
                {
                    System.out.printf("[time %dms] Process %d created (requires %dms CPU time, priority is %d)\n", time, currentProcess.getId(), currentProcess.getTimeRemaining(), currentProcess.getPriority());
                }
                continue;
            }

            if(currentProcess == null)
            {
                // This only happens if the CPU is idle
            }
            else
            {
                currentProcess.run();
                // If this process has terminated
                if(currentProcess.getTimeRemaining() == 0)
                {
                    // True turnaround time is the time taken to run process and the total time waiting
                    currentProcess.setTimeTotal();

                    // Output information
                    System.out.printf("[time %dms] Process %d completed its CPU burst (turnaround time %dms, initial wait time %dms, total wait time %dms)\n", time, currentProcess.getId(), currentProcess.getTimeTotal(), currentProcess.getTimeInitiallyWaiting(), currentProcess.getTimeWaiting());
                    runningProcesses.set(i, null);
                    finishedProcesses.add(currentProcess);
                }
            }

            Process nextProcess = getNextProcess(i);
            if(nextProcess == null)
            {
                continue;
            }

            if(nextProcess != currentProcess)
            {
                if(currentProcess != null)
                {
                    System.out.printf("[time %dms] Context switch (swapping out process %d for process %d in CPU %s)\n", time, currentProcess.getId(), nextProcess.getId(), Character.toString((char) (65 + i)));
                    waitingTimes.set(i, Main.CONTEXT_SWITCH);
                }
                else
                {
                    System.out.printf("[time %dms] Process %d created (requires %dms CPU time, priority is %d)\n", time, nextProcess.getId(), nextProcess.getTimeRemaining(), nextProcess.getPriority());
                }
                runningProcesses.set(i, nextProcess);
            }
        }

        for(Process process : waitingProcesses)
        {
            process.pause();
        }

        time++;
    }

    public Integer getTime()
    {
        return time;
    }

    public void printResults()
    {
        int minTurnaroundTime = Integer.MAX_VALUE;
        double avgTurnaroundTime = 0;
        int maxTurnaroundTime = 0;

        int minInitialWaitTime = Integer.MAX_VALUE;
        double avgInitialWaitTime = 0;
        int maxInitialWaitTime = 0;
    
        int minTotalWaitTime = Integer.MAX_VALUE;
        double avgTotalWaitTime = 0;
        int maxTotalWaitTime = 0;

        for(Process process : finishedProcesses)
        {

            avgTurnaroundTime += process.getTimeTotal();
            avgInitialWaitTime += process.getTimeInitiallyWaiting();
            avgTotalWaitTime += process.getTimeWaiting();


            if (process.getTimeTotal() > maxTurnaroundTime)
            {
                maxTurnaroundTime = process.getTimeTotal();
            }
            if (process.getTimeTotal() < minTurnaroundTime)
            {
                minTurnaroundTime = process.getTimeTotal();
            }

            if (process.getTimeInitiallyWaiting() > maxInitialWaitTime)
            {
                maxInitialWaitTime = process.getTimeInitiallyWaiting();
            }
            if (process.getTimeInitiallyWaiting() < minInitialWaitTime)
            {
                minInitialWaitTime = process.getTimeInitiallyWaiting();
            }

            if (process.getTimeWaiting() > maxTotalWaitTime)
            {
                maxTotalWaitTime = process.getTimeWaiting();
            }
            if (process.getTimeWaiting() < minTotalWaitTime)
            {
                minTotalWaitTime = process.getTimeWaiting();
            }
        }

        int finishedProcessesSize = finishedProcesses.size();
        avgTurnaroundTime /= finishedProcessesSize;
        avgInitialWaitTime /= finishedProcessesSize;
        avgTotalWaitTime /= finishedProcessesSize;

        System.out.printf("Number of CPUs: %d\n", Main.CPU_COUNT);
        System.out.printf("Turnaround time: min %dms; avg %.3fms; max %dms\n", minTurnaroundTime, avgTurnaroundTime, maxTurnaroundTime);
        System.out.printf("Initial wait time: min %dms; avg %.3fms; max %dms\n", minInitialWaitTime, avgInitialWaitTime, maxInitialWaitTime);
        System.out.printf("Total wait time: min %dms; avg %.3fms; max %dms\n\n", minTotalWaitTime, avgTotalWaitTime, maxTotalWaitTime);
    }
}
