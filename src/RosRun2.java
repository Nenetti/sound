
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.PrintStream;
import org.ros.exception.RosRuntimeException;
import org.ros.internal.loader.CommandLineLoader;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

public class RosRun2
{
  public static void printUsage()
  {
    System.err.println("Usage: java -jar my_package.jar com.example.MyNodeMain [args]");
  }
  
  public static void main(String[] argv)
    throws Exception
  {
    if (argv.length == 0)
    {
      printUsage();
      System.exit(1);
    }
    CommandLineLoader loader = new CommandLineLoader(Lists.newArrayList(argv));
    String nodeClassName = loader.getNodeClassName();
    System.out.println("Loading node class: " + loader.getNodeClassName());
    NodeConfiguration nodeConfiguration = loader.build();
    
    NodeMain nodeMain = null;
    try
    {
      nodeMain = loader.loadClass(nodeClassName);
    }
    catch (ClassNotFoundException e)
    {
      throw new RosRuntimeException("Unable to locate node: " + nodeClassName, e);
    }
    catch (InstantiationException e)
    {
      throw new RosRuntimeException("Unable to instantiate node: " + nodeClassName, e);
    }
    catch (IllegalAccessException e)
    {
      throw new RosRuntimeException("Unable to instantiate node: " + nodeClassName, e);
    }
    Preconditions.checkState(nodeMain != null);
    NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
    nodeMainExecutor.execute(nodeMain, nodeConfiguration);
  }
}
