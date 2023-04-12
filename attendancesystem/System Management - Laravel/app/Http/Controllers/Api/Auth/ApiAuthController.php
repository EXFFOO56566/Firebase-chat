<?php

namespace App\Http\Controllers\Api\Auth;

use App\Http\Controllers\Controller;
use App\Models\User;
use Auth;
use Response;

class ApiAuthController extends Controller
{
    public $successStatus = 200;

    /**
     * API data login
     *
     * @return \Illuminate\Http\Response
     */
    public function login()
    {
        if (Auth::attempt(['email' => request('email'), 'password' => request('password')])) {
            $user = Auth::user();
            $data['message'] = "success";
            $data['token'] = $user->createToken('nApp')->accessToken;

            $user = User::find($user->id);
            $data['user'] = $user;
            return response()->json($data, $this->successStatus);
        }

        $data['message'] = "Unauthorised";
        return response()->json($data);
    }
}
