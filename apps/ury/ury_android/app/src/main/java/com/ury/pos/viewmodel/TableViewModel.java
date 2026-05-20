package com.ury.pos.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.ury.pos.model.Room;
import com.ury.pos.model.Table;
import com.ury.pos.repository.TableRepository;

import java.util.ArrayList;
import java.util.List;

public class TableViewModel extends AndroidViewModel {

    public final MutableLiveData<List<Room>>  rooms         = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<List<Table>> tables        = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<Room>        selectedRoom  = new MutableLiveData<>();
    public final MutableLiveData<Boolean>     loading       = new MutableLiveData<>(false);
    public final MutableLiveData<String>      error         = new MutableLiveData<>();

    private final TableRepository repo;

    public TableViewModel(@NonNull Application app) {
        super(app);
        repo = new TableRepository(app);
    }

    public void loadRooms(String branch) {
        loading.setValue(true);
        repo.getRooms(branch, new TableRepository.TableCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> result) {
                rooms.postValue(result);
                loading.postValue(false);
                if (!result.isEmpty()) {
                    selectRoom(result.get(0));
                }
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void selectRoom(Room room) {
        selectedRoom.setValue(room);
        loadTables(room.name);
    }

    public void loadTables(String roomName) {
        loading.setValue(true);
        repo.getTables(roomName, new TableRepository.TableCallback<List<Table>>() {
            @Override
            public void onSuccess(List<Table> result) {
                tables.postValue(result);
                loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void refreshCurrentRoom() {
        Room room = selectedRoom.getValue();
        if (room != null) loadTables(room.name);
    }
}
