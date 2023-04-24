package dao;

import model.Player;

import java.util.List;

public interface PlayerDAO {
    void setOrUpdate(Player player);
    Player getByName(String name);
    List<Player> getAll();
}
