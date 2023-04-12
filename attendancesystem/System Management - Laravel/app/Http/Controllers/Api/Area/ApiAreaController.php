<?php

namespace App\Http\Controllers\Api\Area;

use App\Http\Controllers\Controller;
use App\Models\Area;
use Response;

class ApiAreaController extends Controller
{
    /**
     * API data Area
     *
     * @return \Illuminate\Http\Response
     */
    public function index()
    {
        $dataArea = Area::all('id', 'name');
        if ($dataArea->count() > 0) {
            $data = [
                'message' => 'success',
                'area' => $dataArea
            ];
            return response()->json($data);
        }

        $data = [
            'message' => 'empty',
        ];
        return response()->json($data);
    }
}
