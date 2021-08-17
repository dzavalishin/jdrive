#if 0
#include "stdafx.h"


void MunicipalAirport(Town *tn);

bool MA_OwnerHandler(PlayerID owner);

void MA_Tax(int income, Vehicle *v);

void MA_EditorAddAirport(Town* tn);

bool MA_VehicleIsAtMunicipalAirport(Vehicle *v);

bool MA_WithinVehicleQuota(Station *st);

StationID MA_Find_MS_InVehicleOrders(Vehicle *v, int count);

int MA_VehicleServesMS(Vehicle *v);
