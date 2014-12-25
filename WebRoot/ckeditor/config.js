/**
 * @license Copyright (c) 2003-2014, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function( config ) {
	// Define changes to default configuration here.
	// For complete reference see:
	// http://docs.ckeditor.com/#!/api/CKEDITOR.config

	// The toolbar groups arrangement, optimized for two toolbar rows.
	config.toolbarGroups = [
		{ name: 'clipboard',   groups: [ 'clipboard', 'undo' ] },
		{ name: 'editing',     groups: [ 'find', 'selection', 'spellchecker' ] },
		{ name: 'links' },
		{ name: 'insert' },
		{ name: 'forms' },
		{ name: 'tools' },
		{ name: 'document',	   groups: [ 'mode', 'document', 'doctools' ] },
		{ name: 'others' },
		'/',
		{ name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] },
		{ name: 'paragraph',   groups: [ 'list', 'indent', 'blocks', 'align', 'bidi' ] },
		{ name: 'styles' },
		{ name: 'colors' },
		{ name: 'about' }
	];

	// Remove some buttons provided by the standard plugins, which are
	// not needed in the Standard(s) toolbar.
	config.removeButtons = 'Underline,Subscript,Superscript';

	// Set the most common block elements.
	config.format_tags = 'p;h1;h2;h3;pre';

	// Simplify the dialog windows.
	config.removeDialogTabs = 'image:advanced;link:advanced';
	///Add ckfinder
	
	config.filebrowserBrowseUrl = "/jforum2.1.9/ckfinder/ckfinder.html";
    config.filebrowserImageBrowseUrl = "/jforum2.1.9/ckfinder/ckfinder.html?type=Images";
    config.filebrowserFlashBrowseUrl = "/jforum2.1.9/ckfinder/ckfinder.html?type=Flash";
    config.filebrowserUploadUrl = "/jforum2.1.9/ckfinder/core/connector/java/connector.java?command=QuickUpload&type=Files";
    config.filebrowserImageUploadUrl = "/jforum2.1.9/ckfinder/core/connector/java/connector.java?command=QuickUpload&type=Images";
    config.filebrowserFlashUploadUrl = "/jforum2.1.9/ckfinder/core/connector/java/connector.java?command=QuickUpload&type=Flash";
    config.filebrowserWindowWidth = "1000";
    config.filebrowserWindowHeight = "700";

  // config.language = "zh-cn";
	
	
	
};
