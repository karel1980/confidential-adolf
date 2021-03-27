import {createAction, props} from '@ngrx/store';
import {ErrorResponse, RoomTO} from "./lobby.reducer";

export const setUser = createAction(
  'setUser',
  props<{id: string, name: string}>());

export const goToRoom = createAction(
  'goToRoom',
  props<{roomId: string}>());

export const identified = createAction(
  'identified',
  props<any>()
)

export const requestRoomSync = createAction(
  'requestRoomSync',
  props<any>()
)

export const syncRoom = createAction(
  'syncRoom',
  props<RoomTO>()
)

export const setLastError = createAction(
  'setLastError',
  props<ErrorResponse>()
)

export const userAdded = createAction(
  'UserAdded',
  props<{ id: string, name: string}>()
)
