/* $Id: network_gamelist.h 2701 2005-07-24 14:12:37Z tron $ */

#ifndef NETWORK_GAMELIST_H
#define NETWORK_GAMELIST_H

void NetworkGameListClear(void);
NetworkGameList *NetworkGameListAddItem(uint32 ip, uint16 port);
void NetworkGameListRemoveItem(NetworkGameList *remove);
void NetworkGameListAddQueriedItem(const NetworkGameInfo *info, bool server_online);

#endif /* NETWORK_GAMELIST_H */
