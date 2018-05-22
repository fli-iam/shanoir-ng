package org.shanoir.ng.preclinical.anesthetics.ingredients;

import java.util.List;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "ingredients", description = "the anesthetic ingredients API")
@RequestMapping("/anesthetic/{id}/ingredient")
public interface AnestheticIngredientApi {

    @ApiOperation(value = "Add a new ingredient", notes = "", response = Void.class, tags={ "AnestheticIngredient", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "success returns Ingredient", response = AnestheticIngredient.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = AnestheticIngredient.class),
        @ApiResponse(code = 404, message = "Anesthetic not found", response = Void.class),
        @ApiResponse(code = 409, message = "Already exists - conflict", response = AnestheticIngredient.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = AnestheticIngredient.class) })
    @RequestMapping(value = "",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<AnestheticIngredient> createAnestheticIngredient(@ApiParam(value = "anesthetic id",required=true ) @PathVariable("id") Long id,
    		@ApiParam(value = "anesthetic ingredient to create", required = true) @RequestBody AnestheticIngredient ingredient,
	BindingResult result) throws RestServiceException;


    @ApiOperation(value = "Deletes an ingredient", notes = "", response = Void.class, tags={ "AnestheticIngredient", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid Anesthetic Ingredient id", response = Void.class),
        @ApiResponse(code = 404, message = "Anesthetic not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
    @RequestMapping(value = "/{aiid}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteAnestheticIngredient(@ApiParam(value = "anesthetic id",required=true ) @PathVariable("id") Long id,
    		@ApiParam(value = "Anesthetic Ingredient id",required=true ) @PathVariable("aiid") Long aiid);


    @ApiOperation(value = "Get Anesthetic Ingredient by id", notes = "", response = AnestheticIngredient.class, responseContainer = "List", tags={ "AnestheticIngredient", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An Anesthetic Ingredient  ", response = AnestheticIngredient.class),
        @ApiResponse(code = 404, message = "Anesthetic Ingredient not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = AnestheticIngredient.class) })
    @RequestMapping(value = "/{aiid}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<AnestheticIngredient> getAnestheticIngredientById(@ApiParam(value = "anesthetic id",required=true ) @PathVariable("id") Long id,
    		@ApiParam(value = "Anesthetic Ingredient id",required=true ) @PathVariable("id") Long aiid);
    

    @ApiOperation(value = "List all anesthetic ingredients for given anesthetic", notes = "", response = AnestheticIngredient.class, responseContainer = "List", tags={ "AnestheticIngredient", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of anesthetic ingredients", response = AnestheticIngredient.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = AnestheticIngredient.class),
    	@ApiResponse(code = 404, message = "Anesthetic not found", response = Void.class),
    	@ApiResponse(code = 500, message = "Unexpected error", response = Void.class) })
    @RequestMapping(value = "/all",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<AnestheticIngredient>> getAnestheticIngredients(@ApiParam(value = "anesthetic id",required=true ) @PathVariable("id") Long id)  throws RestServiceException ;


    @ApiOperation(value = "Update an existing anesthetic ingredient", notes = "", response = Void.class, tags={ "AnestheticIngredient", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 404, message = "Anesthetic Ingredient not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
    @RequestMapping(value = "/{aiid}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    ResponseEntity<Void> updateAnestheticIngredient(@ApiParam(value = "anesthetic id",required=true ) @PathVariable("id") Long id,
    		@ApiParam(value = "ID of Anesthetic Ingredient that needs to be updated",required=true ) @PathVariable("aiid") Long aiid,
        @ApiParam(value = "Anesthetic Ingredient object that needs to be updated" ,required=true ) @RequestBody AnestheticIngredient ingredient,
        final BindingResult result) throws RestServiceException;

}
