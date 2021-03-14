import {createAction, props} from '@ngrx/store';

export const setIdentification = createAction(
  'setIdentification',
  props<{id: string, name: string}>());

export const goToRoom = createAction(
  'goToRoom',
  props<{roomId: string}>());

