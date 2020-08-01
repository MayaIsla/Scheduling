class RoundRobinScheduler extends Scheduler
{
    private Integer sliceLength = 100;

    public String getName()
    {
        return "Round Robin";
    }

    public Process getNextProcess(Integer processor)
    {
        Process currentProcess = runningProcesses.get(processor);
        if(currentProcess != null)
        {
            Integer timeRemaining = currentProcess.getTimeRemaining(),
                timeTotal = currentProcess.getTimeTotal(); //Based on algorithm of time splices
            if((timeTotal - timeRemaining) % sliceLength > 0)
            {
                return currentProcess;
            }
            
            addProcess(currentProcess);
            runningProcesses.set(processor, null);
        }
        if(waitingProcesses.size() == 0) return null;
        return waitingProcesses.remove(0);
    }
}