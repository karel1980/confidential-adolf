import {createReducer, on} from '@ngrx/store';
import {setIdentification} from './lobby.actions';

export interface User {
  id: string,
  name: string
}

export interface LobbyState {
  messages: any[]
  user: User
}

const initialState: LobbyState = {
  messages: [],
  user: null
};

export const lobbyReducer = createReducer(
  initialState,
  on(setIdentification, (state, user) => {
    let newState = {...state, user};
    console.log('applying user to state', state, newState)
    return newState;
  }),
);
