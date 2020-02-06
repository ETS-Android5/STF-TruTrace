//
//  DocumentPage.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-11-25.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct DocumentPageCell: View {
    let page: DocumentPage
    let pageIndex : Int
    
    var body: some View {
        NavigationLink(destination: PageDetail(page:page, pageIndex :pageIndex)){
            HStack {
                if page.localImage != nil { ImageViewPageLocalThumbnailWidget(image : page.localImage!)}
                else {
                    ImageViewPageThumbnailWidget(imageUrl: ImageUtils.fetchDocumentPageThumbailURL(pageId : self.page.id))}
                Spacer()
                VStack(alignment: .leading) {
                    Text("\(LocalizationUtils.localizeString(text: "ios_page_cell_page")) \(pageIndex + 1)")
                }
                
            }
        }
    }
}

struct DocumentPageCell_Previews: PreviewProvider {
    static var previews: some View {
        DocumentPageCell(page: DocumentPage(id: 3544, pageNumber: 0, deleted: false ), pageIndex: 0)
    }
}

