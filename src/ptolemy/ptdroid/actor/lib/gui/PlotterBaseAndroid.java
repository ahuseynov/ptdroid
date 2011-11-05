package ptolemy.ptdroid.actor.lib.gui;

import ptolemy.actor.lib.gui.PlotterBase;
import ptolemy.actor.lib.gui.PlotterBaseInterface;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.PlotBoxInterface;
import ptolemy.ptdroid.plot.Plot;
import android.view.ViewGroup;

public class PlotterBaseAndroid implements PlotterBaseInterface {

    private ViewGroup _viewGroup;

    public void init(PlotterBase plotterBase) {
    }

    public void initWindowAndSizeProperties() throws IllegalActionException,
            NameDuplicationException {
    }

    public void exportWindowAndSizeMoML() {
    }

    public void setTableauTitle(String title) {

    }

    public void setFrame(Object frame) {

    }

    public void cleanUp() {

    }

    public void remove() {

    }

    public Object getTableau() {
        return null;
    }

    public Object getFrame() {
        return null;
    }

    public Object getPlatformContainer() {
        return null;
    }

    public void updateSize() {
    }

    public void bringToFront() {
    }

    public void initializeEffigy() throws IllegalActionException {
    }

    public PlotBoxInterface newPlot() {
        return new Plot(_viewGroup.getContext());
    }

    @Override
    public void setPlatformContainer(Object container) {
        _viewGroup = (ViewGroup) container;
    }

    @Override
    public void updateWindowAndSizeAttributes() {
    }

    @Override
    public void removeNullContainer() {
    }

}
