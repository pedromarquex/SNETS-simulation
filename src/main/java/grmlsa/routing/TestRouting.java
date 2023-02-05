package grmlsa.routing;

import java.util.HashMap;

import grmlsa.Route;
import network.Circuit;
import network.Mesh;
import network.Node;

public class TestRouting implements RoutingAlgorithmInterface{

	@Override
	public boolean findRoute(Circuit circuit, Mesh mesh) {
	
        Node source = circuit.getSource();
        Node destination = circuit.getDestination();
        
     

      //  Route route = routesForAllPairs.get(source.getName() + DIV + destination.getName());

      //  if (route != null) {
      //      circuit.setRoute(route);
      //      return true;
      //  }

        return false;
	}

	@Override
	public HashMap<String, Route> getRoutesForAllPairs() {
		// TODO Auto-generated method stub
		return null;
	}

}
