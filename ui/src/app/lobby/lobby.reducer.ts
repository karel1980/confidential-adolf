import {createReducer, on} from '@ngrx/store';
import {goToRoom, identified, requestRoomSync, setUser, syncRoom, userAdded} from './lobby.actions';

export interface LobbyState {
  messages: any[],
  user: User,
  roomId: string,
  room: Room,
  identified: boolean,
  lastSyncRequest: Date
}

export interface User {
  id: string,
  name: string
}

export interface Room {
  users: User[]
}

function createRoom() {
  return {
    users: []
  };
}

const initialState: LobbyState = {
  messages: [],
  user: {
    id: null,
    name: null
  },
  roomId: null,
  room: createRoom(),
  identified: false,
  lastSyncRequest: null
};

export const lobbyReducer = createReducer(
  initialState,
  on(setUser, (state, user) => {
    console.log('setting this in local storage', user);
    localStorage.setItem('user', JSON.stringify(user));
    return ({...state, user});
  }),
  on(goToRoom, (state, {roomId}) => ({...state, roomId: roomId, room: createRoom()})),
  on(identified, (state, a) => ({...state, identified: true})),
  on(requestRoomSync, (state) => ({...state, lastSyncRequest: new Date()})),
  on(syncRoom, (state, room) => ({...state, room})),
  on(userAdded, (state, user) => ({...state, room: { ...state.room, users: [...state.room.users, user ]}}))
);
