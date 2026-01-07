package com.swimdataapp.data;

import com.swimdataapp.model.Swimmer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DataManager {
  private static DataManager instance;
  private final ObservableList<Swimmer> swimmers = FXCollections.observableArrayList();
  private final ObjectProperty<Swimmer> selectedSwimmer = new SimpleObjectProperty<>();

  private DataManager() {
  }

  public static synchronized DataManager getInstance() {
    if (instance == null) {
      instance = new DataManager();
    }
    return instance;
  }

  public ObservableList<Swimmer> getSwimmers() {
    return swimmers;
  }

  public void setSwimmers(java.util.List<Swimmer> swimmerList) {
    swimmers.setAll(swimmerList);
    if (!swimmers.isEmpty()) {
      setSelectedSwimmer(swimmers.get(0));
    } else {
      setSelectedSwimmer(null);
    }
  }

  public Swimmer getSelectedSwimmer() {
    return selectedSwimmer.get();
  }

  public void setSelectedSwimmer(Swimmer swimmer) {
    selectedSwimmer.set(swimmer);
  }

  public ObjectProperty<Swimmer> selectedSwimmerProperty() {
    return selectedSwimmer;
  }
}
